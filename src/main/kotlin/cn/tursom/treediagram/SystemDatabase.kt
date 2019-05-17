package cn.tursom.treediagram

import cn.tursom.database.async.sqlite.AsyncSqliteHelper

object SystemDatabase {
	private val classPath = SystemDatabase::class.java.getResource("/").path!!
	val database = AsyncSqliteHelper("TreeDiagram.db")
}
