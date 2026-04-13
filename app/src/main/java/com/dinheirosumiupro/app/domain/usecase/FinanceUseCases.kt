package com.dinheirosumiupro.app.domain.usecase

import com.dinheirosumiupro.app.domain.model.CategoryTotal
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.MonthBalance
import com.dinheirosumiupro.app.domain.model.MonthlyReport
import java.time.YearMonth

class CalculateMonthBalanceUseCase {
    operator fun invoke(
        entries: List<LedgerEntry>,
        month: YearMonth
    ): MonthBalance {
        val monthEntries = entries.filter { it.referenceMonth == month }
        val income = monthEntries.filter { it.type == EntryType.INCOME }.sumOf { it.amountCents }
        val expenses = monthEntries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amountCents }
        val pending = monthEntries
            .filter { it.type == EntryType.EXPENSE && it.status == EntryStatus.PENDING }
            .sumOf { it.amountCents }

        return MonthBalance(
            month = month,
            totalIncomeCents = income,
            totalExpenseCents = expenses,
            totalPendingCents = pending
        )
    }
}

class BuildMonthlyReportUseCase {
    operator fun invoke(
        entries: List<LedgerEntry>,
        month: YearMonth
    ): MonthlyReport {
        val monthEntries = entries.filter { it.referenceMonth == month }
        val expenseEntries = monthEntries.filter { it.type == EntryType.EXPENSE }
        val incomeEntries = monthEntries.filter { it.type == EntryType.INCOME }

        val byCategory = expenseEntries
            .groupBy { entry -> entry.category.ifBlank { "Sem categoria" } }
            .map { (category, categoryEntries) ->
                CategoryTotal(
                    category = category,
                    totalCents = categoryEntries.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.totalCents }

        val pendingTotal = expenseEntries
            .filter { it.status == EntryStatus.PENDING }
            .sumOf { it.amountCents }

        val topExpenses = expenseEntries
            .sortedByDescending { it.amountCents }
            .take(5)

        return MonthlyReport(
            month = month,
            totalIncomeCents = incomeEntries.sumOf { it.amountCents },
            totalExpenseCents = expenseEntries.sumOf { it.amountCents },
            totalPendingCents = pendingTotal,
            byCategory = byCategory,
            topExpenses = topExpenses
        )
    }
}
