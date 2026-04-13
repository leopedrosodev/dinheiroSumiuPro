package com.dinheirosumiupro.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinheirosumiupro.app.data.local.dao.LedgerDao
import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity

@Database(
    entities = [LedgerEntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
}
