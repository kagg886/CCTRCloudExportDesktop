package top.kagg886.cctr.backend.util

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SortOrder
import top.kagg886.cctr.backend.dao.BaseEntity
import top.kagg886.cctr.backend.service.BaseService

@Serializable
data class PageResult<Entity>(
    val count: Long,
    val data: List<Entity>,
)

suspend fun <Table : IntIdTable, Entity : BaseEntity> BaseService<Table, Entity>.selectById(id: Int): List<Entity> {
    return query { this.where { table.id eq id } }
}

suspend fun <Table : IntIdTable, Entity : BaseEntity> BaseService<Table, Entity>.all(): List<Entity> {
    return query { this }
}

suspend fun <Table : IntIdTable, Entity : BaseEntity> BaseService<Table, Entity>.paged(
    page: Long,
    size: Int,
    reversed: Boolean = false,
): PageResult<Entity> {
    return PageResult(
        count(),
        query { orderBy(table.id, if (reversed) SortOrder.DESC else SortOrder.ASC).limit(size, (page - 1) * size) }
    )
}