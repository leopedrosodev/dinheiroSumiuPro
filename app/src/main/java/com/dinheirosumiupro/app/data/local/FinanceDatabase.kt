package com.dinheirosumiupro.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinheirosumiupro.app.data.local.dao.LedgerDao
import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity
import com.dinheirosumiupro.app.data.local.entity.RecurringEntryTemplateEntity

@Database(
    entities = [LedgerEntryEntity::class, RecurringEntryTemplateEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `recurring_entry_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `amountCents` INTEGER,
                        `type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `counterparty` TEXT,
                        `isActive` INTEGER NOT NULL,
                        `displayOrder` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_recurring_entry_templates_isActive` ON `recurring_entry_templates` (`isActive`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_recurring_entry_templates_displayOrder` ON `recurring_entry_templates` (`displayOrder`)"
                )

                val now = System.currentTimeMillis()
                defaultRecurringTemplates().forEachIndexed { index, template ->
                    val escapedDescription = template.description.replace("'", "''")
                    val escapedCategory = template.category.replace("'", "''")
                    val escapedType = template.type.replace("'", "''")
                    val escapedStatus = template.status.replace("'", "''")
                    val escapedCounterparty = template.counterparty?.replace("'", "''")
                    val amountValue = template.amountCents?.toString() ?: "NULL"
                    val counterpartyValue = escapedCounterparty?.let { "'$it'" } ?: "NULL"

                    database.execSQL(
                        """
                        INSERT INTO `recurring_entry_templates`
                        (`description`, `category`, `amountCents`, `type`, `status`, `counterparty`, `isActive`, `displayOrder`, `createdAtMillis`, `updatedAtMillis`)
                        VALUES ('$escapedDescription', '$escapedCategory', $amountValue, '$escapedType', '$escapedStatus', $counterpartyValue, 1, $index, $now, $now)
                        """.trimIndent()
                    )
                }
            }
        }
    }
}

private data class SeedRecurringTemplate(
    val description: String,
    val category: String,
    val amountCents: Long? = null,
    val type: String = "EXPENSE",
    val status: String = "PENDING",
    val counterparty: String? = null
)

private fun defaultRecurringTemplates(): List<SeedRecurringTemplate> {
    return listOf(
        SeedRecurringTemplate(description = "Aluguel", category = "MORADIA", amountCents = 70_000),
        SeedRecurringTemplate(description = "Internet", category = "CONTAS_FIXAS", amountCents = 10_000),
        SeedRecurringTemplate(description = "Agua", category = "CONTAS_FIXAS", amountCents = 18_000),
        SeedRecurringTemplate(description = "Luz", category = "CONTAS_FIXAS", amountCents = 28_000),
        SeedRecurringTemplate(description = "Celular", category = "CONTAS_FIXAS", amountCents = 6_000),
        SeedRecurringTemplate(description = "Unitv", category = "CONTAS_FIXAS", amountCents = 2_400),
        SeedRecurringTemplate(description = "Gasolina carro", category = "TRANSPORTE", amountCents = 16_000),
        SeedRecurringTemplate(description = "Gasolina moto", category = "TRANSPORTE"),
        SeedRecurringTemplate(description = "Oleo da moto", category = "TRANSPORTE", amountCents = 6_400),
        SeedRecurringTemplate(description = "Faculdade", category = "OUTROS", amountCents = 10_000),
        SeedRecurringTemplate(description = "ChatGPT", category = "DESPESA_NAO_ESSENCIAL", amountCents = 3_700),
        SeedRecurringTemplate(description = "Motoclube", category = "DESPESA_NAO_ESSENCIAL", amountCents = 7_500)
    )
}
