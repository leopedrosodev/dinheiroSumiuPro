package com.dinheirosumiupro.app.data.local.mapper

import com.dinheirosumiupro.app.data.local.entity.LedgerEntryEntity
import com.dinheirosumiupro.app.data.local.entity.RecurringEntryTemplateEntity
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.NewLedgerEntry
import com.dinheirosumiupro.app.domain.model.NewRecurringEntryTemplate
import com.dinheirosumiupro.app.domain.model.RecurringEntryTemplate
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

fun RecurringEntryTemplateEntity.toDomain(): RecurringEntryTemplate {
    return RecurringEntryTemplate(
        id = id,
        description = description,
        category = category,
        amountCents = amountCents,
        type = EntryType.valueOf(type),
        status = EntryStatus.valueOf(status),
        counterparty = counterparty,
        isActive = isActive,
        displayOrder = displayOrder,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

fun NewRecurringEntryTemplate.toEntity(): RecurringEntryTemplateEntity {
    val now = System.currentTimeMillis()
    return RecurringEntryTemplateEntity(
        description = description,
        category = category,
        amountCents = amountCents,
        type = type.name,
        status = status.name,
        counterparty = counterparty,
        isActive = isActive,
        displayOrder = displayOrder,
        createdAtMillis = now,
        updatedAtMillis = now
    )
}

fun RecurringEntryTemplate.toEntity(): RecurringEntryTemplateEntity {
    return RecurringEntryTemplateEntity(
        id = id,
        description = description,
        category = category,
        amountCents = amountCents,
        type = type.name,
        status = status.name,
        counterparty = counterparty,
        isActive = isActive,
        displayOrder = displayOrder,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = System.currentTimeMillis()
    )
}

fun YearMonth.toStorageKey(): String = toString()
