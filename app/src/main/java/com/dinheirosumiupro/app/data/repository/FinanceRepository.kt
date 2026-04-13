package com.dinheirosumiupro.app.data.repository

import com.dinheirosumiupro.app.data.local.dao.LedgerDao
import com.dinheirosumiupro.app.data.local.mapper.toDomain
import com.dinheirosumiupro.app.data.local.mapper.toEntity
import com.dinheirosumiupro.app.data.local.mapper.toStorageKey
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.NewLedgerEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class FinanceRepository(
    private val ledgerDao: LedgerDao
) {
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
}
