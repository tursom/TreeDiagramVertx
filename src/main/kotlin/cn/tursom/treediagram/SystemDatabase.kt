package cn.tursom.treediagram

import cn.tursom.database.sqlite.SQLiteHelper

object SystemDatabase {
	private val classPath = SystemDatabase::class.java.getResource("/").path!!
	val database = SQLiteHelper("TreeDiagram.db")
}
