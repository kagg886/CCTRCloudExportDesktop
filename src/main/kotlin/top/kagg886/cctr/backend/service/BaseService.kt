package top.kagg886.cctr.backend.service

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import top.kagg886.cctr.backend.dao.BaseEntity
import top.kagg886.cctr.backend.dao.database


abstract class BaseService<Table : IntIdTable, Entity : BaseEntity>(val table: Table) {

    init {
        transaction(database) {
            SchemaUtils.create(table)
        }
    }

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(context = Dispatchers.IO, db = database, statement = block)

    suspend fun insert(entity: Entity): Entity? {
        return dbQuery {
            table.insert {
                entity.toDAO(it)
            }.let {
                val s = it.resultedValues?.get(0) ?: return@let null
                return@let toEntity(s)
            }
        }
    }

    suspend fun update(entity: Entity):Boolean {
        return dbQuery {
            table.update(where = {
                table.id eq entity.id
            }) {
                entity.toDAO(it)
            } > 0
        }
    }

    suspend fun count(): Long {
        return dbQuery {
            table.selectAll().count()
        }
    }


    suspend fun query(condition: Query.() -> Query): List<Entity> {
        return dbQuery {
            table.selectAll().condition().map(::toEntity)
        }
    }

    suspend fun delete(condition: Table.(ISqlExpressionBuilder) -> Op<Boolean>): Int {
        return dbQuery {
            table.deleteWhere(op = condition)
        }
    }

    abstract fun toEntity(column: ResultRow): Entity
    abstract fun Entity.toDAO(state: UpdateBuilder<Int>)
}