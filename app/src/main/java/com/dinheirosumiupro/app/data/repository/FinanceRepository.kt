package com.dinheirosumiupro.app.data.repository

import com.dinheirosumiupro.app.data.local.dao.LedgerDao
import com.dinheirosumiupro.app.data.local.mapper.toDomain
import com.dinheirosumiupro.app.data.local.mapper.toEntity
import com.dinheirosumiupro.app.data.local.mapper.toStorageKey
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.NewLedgerEntry
import com.dinheirosumiupro.app.domain.model.NewRecurringEntryTemplate
import com.dinheirosumiupro.app.domain.model.RecurringEntryTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class FinanceRepository(
    private val ledgerDao: LedgerDao
) {
    private val suggestedRecurringAmounts = mapOf(
        "aluguel" to 70_000L,
        "internet" to 10_000L,
        "agua" to 18_000L,
        "luz" to 28_000L,
        "celular" to 6_000L,
        "unitv" to 2_400L,
        "gasolina carro" to 16_000L,
        "oleo da moto" to 6_400L,
        "faculdade" to 10_000L,
        "chatgpt" to 3_700L,
        "motoclube" to 7_500L
    )

    fun observeEntries(): Flow<List<LedgerEntry>> {
        return ledgerDao.observeEntries().map { entities -> entities.map { it.toDomain() } }
    }

    fun observeEntriesByMonth(month: YearMonth): Flow<List<LedgerEntry>> {
        return ledgerDao.observeEntriesByMonth(month.toStorageKey())
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun observePendingEntries(): Flow<List<LedgerEntry>> {
        return ledgerDao.observePendingEntries().map { entities -> entities.map { it.toDomain() } }
    }

    fun observeRecurringTemplates(): Flow<List<RecurringEntryTemplate>> {
        return ledgerDao.observeRecurringTemplates().map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun addEntry(entry: NewLedgerEntry) {
        ledgerDao.insert(entry.toEntity())
    }

    suspend fun updateEntry(entry: LedgerEntry) {
        ledgerDao.update(entry.toEntity())
    }

    suspend fun deleteEntry(entryId: Long) {
        ledgerDao.deleteById(entryId)
    }

    suspend fun updateStatus(entryId: Long, status: EntryStatus) {
        ledgerDao.updateStatus(entryId, status.name)
    }

    suspend fun addRecurringTemplate(template: NewRecurringEntryTemplate) {
        ledgerDao.insertRecurringTemplate(template.toEntity())
    }

    suspend fun updateRecurringTemplate(template: RecurringEntryTemplate) {
        ledgerDao.updateRecurringTemplate(template.toEntity())
    }

    suspend fun deleteRecurringTemplate(templateId: Long) {
        ledgerDao.deleteRecurringTemplateById(templateId)
    }

    suspend fun applySuggestedRecurringTemplateAmounts(): Int {
        val templates = ledgerDao.getRecurringTemplates()
        var updatedCount = 0

        templates.forEach { template ->
            if (template.amountCents != null && template.amountCents > 0) {
                return@forEach
            }

            val suggestedAmount = suggestedRecurringAmounts[template.description.trim().lowercase()]
                ?: return@forEach

            ledgerDao.updateRecurringTemplate(
                template.copy(
                    amountCents = suggestedAmount,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            updatedCount += 1
        }

        return updatedCount
    }

    suspend fun generateEntriesFromRecurringTemplates(month: YearMonth): Int {
        val storageMonth = month.toStorageKey()
        val newEntries = ledgerDao.getActiveRecurringTemplates()
            .filter { template -> template.amountCents != null && template.amountCents > 0 }
            .filter { template ->
                ledgerDao.countMatchingMonthEntries(
                    month = storageMonth,
                    description = template.description,
                    category = template.category,
                    type = template.type
                ) == 0
            }
            .map { template ->
                NewLedgerEntry(
                    description = template.description,
                    category = template.category,
                    amountCents = template.amountCents ?: 0,
                    type = com.dinheirosumiupro.app.domain.model.EntryType.valueOf(template.type),
                    status = com.dinheirosumiupro.app.domain.model.EntryStatus.valueOf(template.status),
                    referenceMonth = month,
                    counterparty = template.counterparty
                ).toEntity()
            }

        if (newEntries.isEmpty()) {
            return 0
        }

        ledgerDao.insertAll(newEntries)
        return newEntries.size
    }
}
