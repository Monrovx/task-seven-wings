package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId?.let { AuthorEntity[it].id }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = prepareQuery(param)

            val total = query.count()

            val sumByType = BudgetEntity.wrapRows(query)
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            query.limit(param.limit, param.offset).orderBy(
                BudgetTable.month to SortOrder.ASC,
                BudgetTable.amount to SortOrder.DESC
            )
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data,
                )
        }
    }

    private fun prepareQuery(param: BudgetYearParam): Query = if (param.authorFullName.isNullOrEmpty()) {
        BudgetTable.select { BudgetTable.year eq param.year }
    } else {
        (BudgetTable innerJoin AuthorTable).select {
                (BudgetTable.year eq param.year) and
                        (AuthorTable.fullName.lowerCase() like "%${param.authorFullName.toLowerCase()}%")
            }
    }
}