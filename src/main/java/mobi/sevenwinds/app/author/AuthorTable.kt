package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.*

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("full_name", 255)
    val creationDate = datetime("date_of_creation")
}

class AuthorEntity(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var creationDate by AuthorTable.creationDate

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullName, creationDate)
    }
}