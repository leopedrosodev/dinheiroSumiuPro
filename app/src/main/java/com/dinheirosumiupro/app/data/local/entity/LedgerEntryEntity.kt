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
