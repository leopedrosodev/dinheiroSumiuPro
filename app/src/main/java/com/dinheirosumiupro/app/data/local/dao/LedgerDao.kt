package com.dinheirosumiupro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY referenceMonth DESC, createdAtMillis DESC")
    fun observeEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE referenceMonth = :month ORDER BY createdAtMillis DESC")
    fun observeEntriesByMonth(month: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE status = 'PENDING' ORDER BY referenceMonth DESC, createdAtMillis DESC")
    fun observePendingEntries(): Flow<List<LedgerEntryEntity>>

    @Insert
    suspend fun insert(entry: LedgerEntryEntity)

    @Update
    suspend fun update(entry: LedgerEntryEntity)

    @Query("DELETE FROM ledger_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("UPDATE ledger_entries SET status = :status WHERE id = :entryId")
    suspend fun updateStatus(entryId: Long, status: String)
}
