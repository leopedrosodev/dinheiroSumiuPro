package com.dinheirosumiupro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity
import com.dinheirosumiupro.app.data.local.entity.RecurringEntryTemplateEntity
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

    @Insert
    suspend fun insertAll(entries: List<LedgerEntryEntity>)

    @Update
    suspend fun update(entry: LedgerEntryEntity)

    @Query("DELETE FROM ledger_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("UPDATE ledger_entries SET status = :status WHERE id = :entryId")
    suspend fun updateStatus(entryId: Long, status: String)

    @Query("SELECT * FROM recurring_entry_templates ORDER BY isActive DESC, displayOrder ASC, description ASC")
    fun observeRecurringTemplates(): Flow<List<RecurringEntryTemplateEntity>>

    @Query("SELECT * FROM recurring_entry_templates ORDER BY isActive DESC, displayOrder ASC, description ASC")
    suspend fun getRecurringTemplates(): List<RecurringEntryTemplateEntity>

    @Query("SELECT * FROM recurring_entry_templates WHERE isActive = 1 ORDER BY displayOrder ASC, description ASC")
    suspend fun getActiveRecurringTemplates(): List<RecurringEntryTemplateEntity>

    @Insert
    suspend fun insertRecurringTemplate(entry: RecurringEntryTemplateEntity)

    @Update
    suspend fun updateRecurringTemplate(entry: RecurringEntryTemplateEntity)

    @Query("DELETE FROM recurring_entry_templates WHERE id = :templateId")
    suspend fun deleteRecurringTemplateById(templateId: Long)

    @Query(
        """
        SELECT COUNT(*)
        FROM ledger_entries
        WHERE referenceMonth = :month
          AND description = :description
          AND category = :category
          AND type = :type
        """
    )
    suspend fun countMatchingMonthEntries(
        month: String,
        description: String,
        category: String,
        type: String
    ): Int
}
