package com.dinheirosumiupro.app.domain.model

import java.time.YearMonth

enum class EntryType {
    INCOME,
    EXPENSE
}

enum class EntryStatus {
    PAID,
    PENDING
}

data class LedgerEntry(
    val id: Long,
    val description: String,
    val category: String,
    val amountCents: Long,
    val type: EntryType,
    val status: EntryStatus,
    val referenceMonth: YearMonth,
    val counterparty: String?,
    val createdAtMillis: Long
)

data class NewLedgerEntry(
    val description: String,
    val category: String,
    val amountCents: Long,
    val type: EntryType,
    val status: EntryStatus,
    val referenceMonth: YearMonth,
    val counterparty: String?
)

data class RecurringEntryTemplate(
    val id: Long,
    val description: String,
    val category: String,
    val amountCents: Long?,
    val type: EntryType,
    val status: EntryStatus,
    val counterparty: String?,
    val isActive: Boolean,
    val displayOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class NewRecurringEntryTemplate(
    val description: String,
    val category: String,
    val amountCents: Long?,
    val type: EntryType,
    val status: EntryStatus,
    val counterparty: String?,
    val isActive: Boolean,
    val displayOrder: Int
)

data class MonthBalance(
    val month: YearMonth,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val totalPendingCents: Long
) {
    val netBalanceCents: Long
        get() = totalIncomeCents - totalExpenseCents

    companion object {
        fun empty(month: YearMonth): MonthBalance {
            return MonthBalance(
                month = month,
                totalIncomeCents = 0,
                totalExpenseCents = 0,
                totalPendingCents = 0
            )
        }
    }
}

data class CategoryTotal(
    val category: String,
    val totalCents: Long
)

data class MonthlyReport(
    val month: YearMonth,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val totalPendingCents: Long,
    val byCategory: List<CategoryTotal>,
    val topExpenses: List<LedgerEntry>
) {
    val netBalanceCents: Long
        get() = totalIncomeCents - totalExpenseCents

    companion object {
        fun empty(month: YearMonth): MonthlyReport {
            return MonthlyReport(
                month = month,
                totalIncomeCents = 0,
                totalExpenseCents = 0,
                totalPendingCents = 0,
                byCategory = emptyList(),
                topExpenses = emptyList()
            )
        }
    }
}
