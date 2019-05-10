package cn.tursom.treediagram.modloader

import cn.tursom.treediagram.modinterface.*
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

object ModManager {
    private val logger = Logger.getLogger("ModManager")!!
    private val systemModMap = ConcurrentHashMap<String, BaseMod>()
    private val routeMap = ConcurrentHashMap<String, BaseMod>()

    private val userModMapMap: Hashtable<String, Hashtable<String, BaseMod>> = Hashtable()

    fun loadBaseMod(router: Router) {
        //加载系统模组
        arrayOf(
            cn.tursom.treediagram.basemod.Echo(),
            cn.tursom.treediagram.basemod.Email(),
            cn.tursom.treediagram.basemod.GroupEmail(),
            cn.tursom.treediagram.basemod.MultipleEmail(),
            cn.tursom.treediagram.basemod.ModLoader(),
            cn.tursom.treediagram.basemod.Upload(),
            cn.tursom.treediagram.basemod.MultipleUpload(),
            cn.tursom.treediagram.basemod.GetUploadFileList(),
            cn.tursom.treediagram.basemod.Register(),
            cn.tursom.treediagram.basemod.Login()
        ).forEach {
            it.router = router
            loadMod(it)
        }
    }

    fun getSystemMod(modName: String) = systemModMap[modName]

    fun getUserMod(user: String, modName: String) = userModMapMap[user]?.get(modName)

    /**
     * 加载模组
     * 将模组的注册信息加载进系统中
     */
    internal fun loadMod(mod: BaseMod) {
        //输出日志信息
        logger.info("loading mod: ${mod::class.java.name}")

        val router = mod.router!!
        //调用模组的初始化函数
        mod.init()
        //将模组的信息加载到系统中
        //记得销毁被替代的模组
        try {
            systemModMap[mod.modName]?.destroy()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        systemModMap[mod.modName] = mod
        systemModMap[mod.modName.split('.').last()] = mod


        val modClass = mod.javaClass
        val modPath = modClass.getAnnotation(ModPath::class.java)

        val fullPath = "/mod/system/${mod.modName}"
        addRoute(router, fullPath, mod)
        if (modPath != null) {
            modPath.path.forEach {
                val path = "/mod/system/$it"
                if (path != fullPath) addRoute(router, path, mod)
            }
        } else {
            val path = "/mod/system/${mod.modName.split('.').last()}"
            if (path != fullPath) addRoute(router, path, mod)
        }

    }

    /**
     * 加载模组
     * 将模组的注册信息加载进系统中
     */
    fun loadMod(user: String, mod: BaseMod): String {
        //输出日志信息
        logger.info("loading mod: ${mod::class.java.name}\nuser: $user")

        val router = mod.router!!

        //调用模组的初始化函数
        mod.init()
        //将模组的信息加载到系统中
        val userModMap = (userModMapMap[user] ?: run {
            val modMap = Hashtable<String, BaseMod>()
            userModMapMap[user] = modMap
            modMap
        })
        //记得销毁被替代的模组
        try {
            userModMap[mod.modName]?.destroy()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        userModMap[mod.modName] = mod
        userModMap[mod.simpName] = mod

        val modClass = mod.javaClass
        val modPath = modClass.getAnnotation(ModPath::class.java)

        val fullPath = "/mod/user/$user/${mod.modName}"
        addRoute(router, fullPath, mod)

        if (modPath != null) {
            modPath.path.forEach {
                val path = "/mod/user/$user/$it"
                if (path != fullPath) addRoute(router, path, mod)
            }
        } else {
            val path = "/mod/user/$user/${mod.modName.split('.').last()}"
            if (path != fullPath) addRoute(router, path, mod)
        }

        return mod.modName
    }

    fun loadMod(configData: ClassData, user: String? = null, rootPath: String? = null, router: Router): Boolean {
        //要加载的类名
        val className: Array<String> = configData.classname!!
        //类加载器
        val myClassLoader: ClassLoader? = try {
            val file = if (rootPath == null) {
                File(configData.path!!)
            } else {
                File(rootPath + configData.path!!)
            }
            //如果文件不存在，抛出一个文件不存在异常
            if (!file.exists()) throw FileNotFoundException()
            val url = file.toURI().toURL()
            URLClassLoader(arrayOf(url), Thread.currentThread().contextClassLoader)
        } catch (e: Exception) {
            //从文件加载模组失败，尝试从网络加载模组
            URLClassLoader(arrayOf(URL(configData.url!!)), Thread.currentThread().contextClassLoader)
        }
        //是否所有的模组都加载成功
        var allSuccessful = true
        className.forEach { className1 ->
            try {
                //获取一个指定模组的对象
                val modClass = myClassLoader!!.loadClass(className1)
                val modObject = modClass.getConstructor().newInstance() as BaseMod
                modObject.router = router
                //加载模组
                if (user == null)
                    loadMod(modObject)
                else {
                    removeMod(user, modObject.modName)
                    removeMod(user, modObject.modName.split(".").last())
                    loadMod(user, modObject)
                }
            } catch (e: NoSuchMethodException) {
                //如果失败，将标志位置否
                allSuccessful = false
            }
        }
        return allSuccessful
    }

    /**
     * 卸载模组
     */
    fun removeMod(user: String, mod: String) {
        //输出日志信息
        logger.info("remove mod: $mod\nuser: $user")
        //找到用户的模组地图
        val userModMap = userModMapMap[user] ?: return
        //找到要卸载的模组
        val modObject = userModMap[mod] ?: return
        //调用卸载方法
        modObject.destroy()
        //删除模组的引用
        userModMap.remove(modObject.modName)
        //删除模组根据简称的引用
        if (modObject === userModMap[modObject.modName.split('.').last()])
            userModMap.remove(modObject.modName.split('.').last())

        val router = modObject.router!!
        delRoute(router, "user/$user", modObject)
    }

    /**
     * 卸载模组
     */
    fun removeMod(mod: String) {
        //输出日志信息
        logger.info("remove system mod: $mod")
        //找到要卸载的模组
        val modObject = systemModMap[mod] ?: return
        //调用卸载方法
        modObject.destroy()
        //删除模组的引用
        systemModMap.remove(modObject.modName)
        //删除模组根据简称的引用
        if (modObject === systemModMap[modObject.modName.split('.').last()])
            systemModMap.remove(modObject.modName.split('.').last())

        val router = modObject.router!!
        delRoute(router, "system", modObject)
    }

    private fun addRoute(router: Router, path: String, mod: BaseMod) {
        val modClass = mod.javaClass
        router.delete(path)

        val route = when {
            modClass.getAnnotation(GetMod::class.java) != null -> router.get(path)
            modClass.getAnnotation(PostMod::class.java) != null -> router.post(path)
            else -> router.route(path)
        }

        val needBody = modClass.getAnnotation(NeedBody::class.java)
        if (needBody != null) {
            val bodyHandler = BodyHandler.create()
            if (needBody.maxSize > 0) bodyHandler.setBodyLimit(needBody.maxSize)
            route.handler(bodyHandler)
        }
        if (modClass.getAnnotation(NoBlocking::class.java) != null) {
            route.handler(mod)
        } else {
            route.blockingHandler(mod)
        }
        routeMap[path] = mod
    }


    private fun delRoute(router: Router, subPath: String, mod: BaseMod) {
        val modClass = mod.javaClass

        val fullPath = "/mod/$subPath/${mod.modName}"
        if (routeMap[fullPath] === mod)
            if (modClass.getAnnotation(NeedBody::class.java) != null) {
                router.delete("$fullPath/*")
                router.delete(fullPath)
            } else {
                router.delete(fullPath)
            }

        val modPath = modClass.getAnnotation(ModPath::class.java)
        if (modPath != null) {
            modPath.path.forEach {
                val path = "/mod/$subPath/$it"
                if (path != fullPath && routeMap[path] === mod)
                    router.delete(path)
            }
        } else {
            val path = "/mod/$subPath/${mod.modName.split('.').last()}"
            if (path != fullPath && routeMap[path] === mod)
                router.delete(path)
        }
    }
}