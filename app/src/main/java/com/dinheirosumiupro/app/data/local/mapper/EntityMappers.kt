package com.dinheirosumiupro.app.data.local.mapper

import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.NewLedgerEntry
import java.time.YearMonth

fun LedgerEntryEntity.toDomain(): LedgerEntry {
    return LedgerEntry(
        id = id,
        description = description,
        category = category,
        amountCents = amountCents,
        type = EntryType.valueOf(type),
        status = EntryStatus.valueOf(status),
        referenceMonth = YearMonth.parse(referenceMonth),
        counterparty = counterparty,
        createdAtMillis = createdAtMillis
    )
}

fun NewLedgerEntry.toEntity(): LedgerEntryEntity {
    return LedgerEntryEntity(
        description = description,
        category = category,
        amountCents = amountCents,
        type = type.name,
        status = status.name,
        referenceMonth = referenceMonth.toStorageKey(),
        counterparty = counterparty,
        createdAtMillis = System.currentTimeMillis()
    )
}

fun LedgerEntry.toEntity(): LedgerEntryEntity {
    return LedgerEntryEntity(
        id = id,
        description = description,
        category = category,
        amountCents = amountCents,
        type = type.name,
        status = status.name,
        referenceMonth = referenceMonth.toStorageKey(),
        counterparty = counterparty,
        createdAtMillis = createdAtMillis
    )
}

fun YearMonth.toStorageKey(): String = toString()
