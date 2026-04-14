package com.dinheirosumiupro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
    indices = [
        Index(value = ["referenceMonth"]),
        Index(value = ["status"])
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val category: String,
    val amountCents: Long,
    val type: String,
    val status: String,
    val referenceMonth: String,
    val counterparty: String?,
    val createdAtMillis: Long
)

@Entity(
    tableName = "recurring_entry_templates",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["displayOrder"])
    ]
)
data class RecurringEntryTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val category: String,
    val amountCents: Long?,
    val type: String,
    val status: String,
    val counterparty: String?,
    val isActive: Boolean,
    val displayOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
