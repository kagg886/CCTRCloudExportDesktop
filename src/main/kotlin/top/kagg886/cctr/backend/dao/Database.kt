package top.kagg886.cctr.backend.dao

import org.jetbrains.exposed.sql.Database
import org.sqlite.JDBC
import top.kagg886.cctr.desktop.util.root_file

val database = Database.connect(
    url= "jdbc:sqlite:${root_file}/cctr.db",
    driver = JDBC::class.java.name
)