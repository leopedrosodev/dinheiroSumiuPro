package com.dinheirosumiupro.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinheirosumiupro.app.data.repository.FinanceRepository
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.NewLedgerEntry
import com.dinheirosumiupro.app.domain.usecase.BuildMonthlyReportUseCase
import com.dinheirosumiupro.app.domain.usecase.CalculateMonthBalanceUseCase
import com.dinheirosumiupro.app.presentation.common.FinanceCategories
import com.dinheirosumiupro.app.presentation.common.formatCentsToInput
import com.dinheirosumiupro.app.presentation.common.parseCurrencyToCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

private data class FinanceLocalState(
    val selectedTab: FinanceTab = FinanceTab.BALANCO,
    val selectedEntriesMonth: YearMonth = YearMonth.now(),
    val selectedReportMonth: YearMonth = YearMonth.now(),
    val showEntryDialog: Boolean = false,
    val editingEntryId: Long? = null,
    val form: EntryFormState = EntryFormState(),
    val feedbackMessage: String? = null
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val calculateMonthBalanceUseCase: CalculateMonthBalanceUseCase,
    private val buildMonthlyReportUseCase: BuildMonthlyReportUseCase
) : ViewModel() {
    private val localState = MutableStateFlow(FinanceLocalState())
    private var latestEntries: List<LedgerEntry> = emptyList()

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.observeEntries(),
        localState
    ) { entries, local ->
        latestEntries = entries

        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val availableMonths = buildSet {
            add(currentMonth)
            add(previousMonth)
            entries.forEach { add(it.referenceMonth) }
            add(local.form.referenceMonth)
            add(local.selectedEntriesMonth)
            add(local.selectedReportMonth)
        }.toList().sortedDescending()

        val selectedEntriesMonth = if (availableMonths.contains(local.selectedEntriesMonth)) {
            local.selectedEntriesMonth
        } else {
            availableMonths.first()
        }

        val selectedReportMonth = if (availableMonths.contains(local.selectedReportMonth)) {
            local.selectedReportMonth
        } else {
            availableMonths.first()
        }

        val entriesForSelectedMonth = entries
            .filter { it.referenceMonth == selectedEntriesMonth }
            .sortedByDescending { it.createdAtMillis }

        val reportEntries = entries
            .filter { it.referenceMonth == selectedReportMonth }
            .sortedByDescending { it.createdAtMillis }

        val currentMonthEntries = entries.filter { it.referenceMonth == currentMonth }
        val currentMonthExpenses = currentMonthEntries.filter { it.type == EntryType.EXPENSE }
        val currentMonthIncome = currentMonthEntries.filter { it.type == EntryType.INCOME }

        val salaryCents = currentMonthIncome
            .filter { it.category == FinanceCategories.SALARIO }
            .sumOf { it.amountCents }

        val nonEssentialExpenseCents = currentMonthExpenses
            .filter { it.category == FinanceCategories.DESPESA_NAO_ESSENCIAL }
            .sumOf { it.amountCents }

        val investmentCents = currentMonthEntries
            .filter { it.category == FinanceCategories.INVESTIMENTO }
            .sumOf { it.amountCents }

        val pendingEntries = entries
            .filter { it.status == EntryStatus.PENDING && it.type == EntryType.EXPENSE }
            .sortedWith(
                compareByDescending<LedgerEntry> { it.referenceMonth }
                    .thenByDescending { it.createdAtMillis }
            )

        FinanceUiState(
            selectedTab = local.selectedTab,
            selectedEntriesMonth = selectedEntriesMonth,
            selectedReportMonth = selectedReportMonth,
            availableMonths = availableMonths,
            showEntryDialog = local.showEntryDialog,
            isEditingEntry = local.editingEntryId != null,
            form = local.form,
            currentMonthBalance = calculateMonthBalanceUseCase(entries, currentMonth),
            previousMonthBalance = calculateMonthBalanceUseCase(entries, previousMonth),
            currentMonthTotalSpentCents = currentMonthExpenses.sumOf { it.amountCents },
            currentMonthSalaryCents = salaryCents,
            currentMonthNonEssentialExpenseCents = nonEssentialExpenseCents,
            currentMonthInvestmentCents = investmentCents,
            entriesForSelectedMonth = entriesForSelectedMonth,
            reportEntries = reportEntries,
            pendingEntries = pendingEntries,
            monthlyReport = buildMonthlyReportUseCase(entries, selectedReportMonth),
            feedbackMessage = local.feedbackMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    fun onEvent(event: FinanceUiEvent) {
        when (event) {
            is FinanceUiEvent.SelectTab -> {
                localState.update { it.copy(selectedTab = event.tab) }
            }

            is FinanceUiEvent.SelectEntriesMonth -> {
                localState.update { it.copy(selectedEntriesMonth = event.month) }
            }

            is FinanceUiEvent.SelectReportMonth -> {
                localState.update { it.copy(selectedReportMonth = event.month) }
            }

            FinanceUiEvent.OpenAddDialog -> {
                val baseMonth = localState.value.selectedEntriesMonth
                localState.update {
                    it.copy(
                        showEntryDialog = true,
                        editingEntryId = null,
                        form = EntryFormState(referenceMonth = baseMonth)
                    )
                }
            }

            is FinanceUiEvent.OpenEditDialog -> {
                localState.update {
                    it.copy(
                        showEntryDialog = true,
                        editingEntryId = event.entry.id,
                        form = EntryFormState(
                            description = event.entry.description,
                            category = event.entry.category,
                            amountInput = formatCentsToInput(event.entry.amountCents),
                            type = event.entry.type,
                            status = event.entry.status,
                            referenceMonth = event.entry.referenceMonth,
                            counterparty = event.entry.counterparty.orEmpty()
                        )
                    )
                }
            }

            FinanceUiEvent.CloseEntryDialog -> {
                localState.update {
                    it.copy(
                        showEntryDialog = false,
                        editingEntryId = null,
                        form = EntryFormState(referenceMonth = it.selectedEntriesMonth)
                    )
                }
            }

            is FinanceUiEvent.ChangeDescription -> {
                localState.update { it.copy(form = it.form.copy(description = event.value)) }
            }

            is FinanceUiEvent.ChangeCategory -> {
                localState.update { it.copy(form = it.form.copy(category = event.value)) }
            }

            is FinanceUiEvent.ChangeAmount -> {
                localState.update { it.copy(form = it.form.copy(amountInput = event.value)) }
            }

            is FinanceUiEvent.ChangeType -> {
                localState.update { state ->
                    val allowedCategories = FinanceCategories.optionsForType(event.value)
                    val adjustedCategory = state.form.category
                        .takeIf { it in allowedCategories }
                        ?: allowedCategories.first()
                    state.copy(
                        form = state.form.copy(
                            type = event.value,
                            category = adjustedCategory
                        )
                    )
                }
            }

            is FinanceUiEvent.ChangeStatus -> {
                localState.update { it.copy(form = it.form.copy(status = event.value)) }
            }

            is FinanceUiEvent.ChangeReferenceMonth -> {
                localState.update { it.copy(form = it.form.copy(referenceMonth = event.value)) }
            }

            is FinanceUiEvent.ChangeCounterparty -> {
                localState.update { it.copy(form = it.form.copy(counterparty = event.value)) }
            }

            FinanceUiEvent.SaveEntry -> saveEntry()
            is FinanceUiEvent.DeleteEntry -> deleteEntry(event.entryId)
            is FinanceUiEvent.UpdateEntryStatus -> updateEntryStatus(event.entryId, event.status)
            is FinanceUiEvent.ShowFeedback -> {
                localState.update { it.copy(feedbackMessage = event.message) }
            }

            FinanceUiEvent.ClearFeedback -> {
                localState.update { it.copy(feedbackMessage = null) }
            }
        }
    }

    private fun saveEntry() {
        val local = localState.value
        val form = local.form
        val amountCents = parseCurrencyToCents(form.amountInput)

        if (form.description.isBlank()) {
            localState.update { it.copy(feedbackMessage = "Descrição é obrigatória") }
            return
        }

        if (amountCents == null) {
            localState.update { it.copy(feedbackMessage = "Valor inválido. Exemplo: 700,00") }
            return
        }

        val allowedCategories = FinanceCategories.optionsForType(form.type)
        val category = form.category.takeIf { it in allowedCategories } ?: FinanceCategories.OUTROS
        val counterparty = form.counterparty.trim().takeIf { it.isNotBlank() }

        viewModelScope.launch {
            val editingEntryId = local.editingEntryId

            if (editingEntryId == null) {
                repository.addEntry(
                    NewLedgerEntry(
                        description = form.description.trim(),
                        category = category,
                        amountCents = amountCents,
                        type = form.type,
                        status = form.status,
                        referenceMonth = form.referenceMonth,
                        counterparty = counterparty
                    )
                )

                localState.update {
                    it.copy(
                        showEntryDialog = false,
                        editingEntryId = null,
                        form = EntryFormState(referenceMonth = form.referenceMonth),
                        selectedEntriesMonth = form.referenceMonth,
                        selectedReportMonth = form.referenceMonth,
                        feedbackMessage = "Lançamento salvo com sucesso"
                    )
                }
                return@launch
            }

            val existing = latestEntries.firstOrNull { it.id == editingEntryId }
            if (existing == null) {
                localState.update { it.copy(feedbackMessage = "Não foi possível atualizar o item") }
                return@launch
            }

            repository.updateEntry(
                existing.copy(
                    description = form.description.trim(),
                    category = category,
                    amountCents = amountCents,
                    type = form.type,
                    status = form.status,
                    referenceMonth = form.referenceMonth,
                    counterparty = counterparty
                )
            )

            localState.update {
                it.copy(
                    showEntryDialog = false,
                    editingEntryId = null,
                    form = EntryFormState(referenceMonth = form.referenceMonth),
                    selectedEntriesMonth = form.referenceMonth,
                    selectedReportMonth = form.referenceMonth,
                    feedbackMessage = "Lançamento atualizado"
                )
            }
        }
    }

    private fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            localState.update { it.copy(feedbackMessage = "Item removido") }
        }
    }

    private fun updateEntryStatus(entryId: Long, status: EntryStatus) {
        viewModelScope.launch {
            repository.updateStatus(entryId, status)
            localState.update {
                it.copy(
                    feedbackMessage = if (status == EntryStatus.PAID) {
                        "Pendência marcada como paga"
                    } else {
                        "Item marcado como pendente"
                    }
                )
            }
        }
    }
}
