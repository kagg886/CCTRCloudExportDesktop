package top.kagg886.cctr.backend.dao

import org.jetbrains.exposed.sql.Database
import org.sqlite.JDBC

val database = Database.connect(
    url= "jdbc:sqlite:cctr-desktop/cctr.db",
    driver = JDBC::class.java.name
)