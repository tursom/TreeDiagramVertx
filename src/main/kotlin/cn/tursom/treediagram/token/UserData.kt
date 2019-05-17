package cn.tursom.treediagram.token

import cn.tursom.database.annotation.NotNull
import cn.tursom.database.annotation.TableName
import cn.tursom.database.async.AsyncSqlAdapter
import cn.tursom.database.clauses.clause
import cn.tursom.database.sqlite.SQLiteHelper
import cn.tursom.tools.sha256
import cn.tursom.treediagram.SystemDatabase
import kotlinx.coroutines.runBlocking

@TableName("users")
data class UserData(
    @NotNull val username: String,
    @NotNull val password: String,
    @NotNull val level: String
)

private val database = SQLiteHelper("${UserData::class.java.getResource("/").path!!}TreeDiagram.db")

internal val userTable = runBlocking {
    SystemDatabase.database.createTable(UserData::class.java)
    "users"
}

internal suspend fun findUser(username: String): UserData? {
    val adapter = AsyncSqlAdapter(UserData::class.java)
    SystemDatabase.database.select(adapter, null, where = clause {
        !UserData::username equal !username
    }, maxCount = 1)
    return if (adapter.count() == 0) null
    else adapter[0]
}

suspend fun tryLogin(username: String, password: String): Boolean {
    //查询用户数据
    val userData = findUser(username)
    return "$username$password$username$password".sha256() == userData?.password
}