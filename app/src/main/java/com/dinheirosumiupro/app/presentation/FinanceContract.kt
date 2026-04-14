package com.dinheirosumiupro.app.presentation

import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.MonthBalance
import com.dinheirosumiupro.app.domain.model.MonthlyReport
import com.dinheirosumiupro.app.domain.model.RecurringEntryTemplate
import com.dinheirosumiupro.app.presentation.common.FinanceCategories
import java.time.YearMonth

enum class FinanceTab {
    BALANCO,
    GASTOS,
    PENDENCIAS,
    RELATORIO
}

data class EntryFormState(
    val description: String = "",
    val category: String = FinanceCategories.OUTROS,
    val amountInput: String = "",
    val type: EntryType = EntryType.EXPENSE,
    val status: EntryStatus = EntryStatus.PAID,
    val referenceMonth: YearMonth = YearMonth.now(),
    val counterparty: String = ""
)

data class RecurringTemplateFormState(
    val description: String = "",
    val category: String = FinanceCategories.OUTROS,
    val amountInput: String = "",
    val type: EntryType = EntryType.EXPENSE,
    val status: EntryStatus = EntryStatus.PENDING,
    val counterparty: String = "",
    val isActive: Boolean = true
)

data class FinanceUiState(
    val selectedTab: FinanceTab = FinanceTab.BALANCO,
    val selectedEntriesMonth: YearMonth = YearMonth.now(),
    val selectedReportMonth: YearMonth = YearMonth.now(),
    val availableMonths: List<YearMonth> = listOf(YearMonth.now(), YearMonth.now().minusMonths(1)),
    val showEntryDialog: Boolean = false,
    val isEditingEntry: Boolean = false,
    val form: EntryFormState = EntryFormState(),
    val recurringTemplates: List<RecurringEntryTemplate> = emptyList(),
    val showRecurringTemplateDialog: Boolean = false,
    val isEditingRecurringTemplate: Boolean = false,
    val recurringTemplateForm: RecurringTemplateFormState = RecurringTemplateFormState(),
    val currentMonthBalance: MonthBalance = MonthBalance.empty(YearMonth.now()),
    val previousMonthBalance: MonthBalance = MonthBalance.empty(YearMonth.now().minusMonths(1)),
    val currentMonthTotalSpentCents: Long = 0,
    val currentMonthSalaryCents: Long = 0,
    val currentMonthNonEssentialExpenseCents: Long = 0,
    val currentMonthInvestmentCents: Long = 0,
    val entriesForSelectedMonth: List<LedgerEntry> = emptyList(),
    val reportEntries: List<LedgerEntry> = emptyList(),
    val pendingEntries: List<LedgerEntry> = emptyList(),
    val monthlyReport: MonthlyReport = MonthlyReport.empty(YearMonth.now()),
    val feedbackMessage: String? = null
)

sealed interface FinanceUiEvent {
    data class SelectTab(val tab: FinanceTab) : FinanceUiEvent
    data class SelectEntriesMonth(val month: YearMonth) : FinanceUiEvent
    data class SelectReportMonth(val month: YearMonth) : FinanceUiEvent

    data object OpenAddDialog : FinanceUiEvent
    data class OpenEditDialog(val entry: LedgerEntry) : FinanceUiEvent
    data object CloseEntryDialog : FinanceUiEvent
    data object OpenAddRecurringTemplateDialog : FinanceUiEvent
    data class OpenEditRecurringTemplateDialog(val template: RecurringEntryTemplate) : FinanceUiEvent
    data object CloseRecurringTemplateDialog : FinanceUiEvent

    data class ChangeDescription(val value: String) : FinanceUiEvent
    data class ChangeCategory(val value: String) : FinanceUiEvent
    data class ChangeAmount(val value: String) : FinanceUiEvent
    data class ChangeType(val value: EntryType) : FinanceUiEvent
    data class ChangeStatus(val value: EntryStatus) : FinanceUiEvent
    data class ChangeReferenceMonth(val value: YearMonth) : FinanceUiEvent
    data class ChangeCounterparty(val value: String) : FinanceUiEvent
    data class ChangeRecurringTemplateDescription(val value: String) : FinanceUiEvent
    data class ChangeRecurringTemplateCategory(val value: String) : FinanceUiEvent
    data class ChangeRecurringTemplateAmount(val value: String) : FinanceUiEvent
    data class ChangeRecurringTemplateType(val value: EntryType) : FinanceUiEvent
    data class ChangeRecurringTemplateStatus(val value: EntryStatus) : FinanceUiEvent
    data class ChangeRecurringTemplateCounterparty(val value: String) : FinanceUiEvent
    data class ChangeRecurringTemplateActive(val value: Boolean) : FinanceUiEvent

    data object SaveEntry : FinanceUiEvent
    data object SaveRecurringTemplate : FinanceUiEvent
    data object ApplySuggestedRecurringTemplateAmounts : FinanceUiEvent
    data class DeleteEntry(val entryId: Long) : FinanceUiEvent
    data class UpdateEntryStatus(val entryId: Long, val status: EntryStatus) : FinanceUiEvent
    data class DeleteRecurringTemplate(val templateId: Long) : FinanceUiEvent
    data class UpdateRecurringTemplateActive(val templateId: Long, val isActive: Boolean) : FinanceUiEvent
    data class GenerateEntriesForMonth(val month: YearMonth) : FinanceUiEvent
    data class ShowFeedback(val message: String) : FinanceUiEvent

    data object ClearFeedback : FinanceUiEvent
}
