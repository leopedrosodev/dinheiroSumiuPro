package com.dinheirosumiupro.app.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dinheirosumiupro.app.R
import com.dinheirosumiupro.app.core.common.simpleViewModelFactory
import com.dinheirosumiupro.app.core.di.AppContainer
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.MonthBalance
import com.dinheirosumiupro.app.domain.model.MonthlyReport
import com.dinheirosumiupro.app.domain.model.RecurringEntryTemplate
import com.dinheirosumiupro.app.presentation.common.FinanceCategories
import com.dinheirosumiupro.app.presentation.common.ReportExporter
import com.dinheirosumiupro.app.presentation.common.formatCurrencyFromCents
import com.dinheirosumiupro.app.presentation.common.formatMonthYear
import com.dinheirosumiupro.app.presentation.common.formatSignedCurrencyFromCents
import com.dinheirosumiupro.app.ui.theme.DinheiroSumiuProTheme
import com.dinheirosumiupro.app.ui.theme.ExpenseRed
import com.dinheirosumiupro.app.ui.theme.IncomeGreen
import com.dinheirosumiupro.app.ui.theme.PendingOrange
import kotlinx.coroutines.delay
import java.time.YearMonth

private data class TabItem(
    val tab: FinanceTab,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val tabItems = listOf(
    TabItem(FinanceTab.BALANCO, R.string.aba_balanco, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TabItem(FinanceTab.GASTOS, R.string.aba_gastos, Icons.Filled.Payments, Icons.Outlined.Payments),
    TabItem(FinanceTab.PENDENCIAS, R.string.aba_pendencias, Icons.Filled.MoneyOff, Icons.Outlined.MoneyOff),
    TabItem(FinanceTab.RELATORIO, R.string.aba_relatorio, Icons.Filled.Assessment, Icons.Outlined.TaskAlt)
)

@Composable
private fun IntroSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.splash_prompt),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(160.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceRoot(appContainer: AppContainer) {
    val financeViewModel: FinanceViewModel = viewModel(
        factory = remember(appContainer) {
            simpleViewModelFactory {
                FinanceViewModel(
                    repository = appContainer.financeRepository,
                    calculateMonthBalanceUseCase = appContainer.calculateMonthBalanceUseCase,
                    buildMonthlyReportUseCase = appContainer.buildMonthlyReportUseCase
                )
            }
        }
    )

    val state by financeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbars = remember { SnackbarHostState() }
    val context = LocalContext.current

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) {
            financeViewModel.onEvent(FinanceUiEvent.ShowFeedback(context.getString(R.string.feedback_export_cancelado)))
            return@rememberLauncherForActivityResult
        }

        val result = runCatching {
            val csv = ReportExporter.buildMonthlyCsv(state.monthlyReport, state.reportEntries)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(csv.toByteArray(Charsets.UTF_8))
            } ?: error("Falha ao abrir destino")
        }

        val message = result.fold(
            onSuccess = { context.getString(R.string.feedback_export_csv_ok) },
            onFailure = { context.getString(R.string.feedback_export_erro, it.message.orEmpty()) }
        )
        financeViewModel.onEvent(FinanceUiEvent.ShowFeedback(message))
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) {
            financeViewModel.onEvent(FinanceUiEvent.ShowFeedback(context.getString(R.string.feedback_export_cancelado)))
            return@rememberLauncherForActivityResult
        }

        val result = runCatching {
            val pdfBytes = ReportExporter.buildMonthlyPdf(state.monthlyReport, state.reportEntries)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(pdfBytes)
            } ?: error("Falha ao abrir destino")
        }

        val message = result.fold(
            onSuccess = { context.getString(R.string.feedback_export_pdf_ok) },
            onFailure = { context.getString(R.string.feedback_export_erro, it.message.orEmpty()) }
        )
        financeViewModel.onEvent(FinanceUiEvent.ShowFeedback(message))
    }

    LaunchedEffect(state.feedbackMessage) {
        val message = state.feedbackMessage ?: return@LaunchedEffect
        snackbars.showSnackbar(message)
        financeViewModel.onEvent(FinanceUiEvent.ClearFeedback)
    }

    DinheiroSumiuProTheme {
        var showSplash by rememberSaveable { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(1800)
            showSplash = false
        }

        if (showSplash) {
            IntroSplashScreen()
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbars) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_app),
                                    contentDescription = stringResource(id = R.string.app_name),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(text = stringResource(id = R.string.app_name))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        tabItems.forEach { item ->
                            val selected = state.selectedTab == item.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { financeViewModel.onEvent(FinanceUiEvent.SelectTab(item.tab)) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(id = item.titleRes)
                                    )
                                },
                                label = { Text(text = stringResource(id = item.titleRes)) }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (state.selectedTab == FinanceTab.GASTOS || state.selectedTab == FinanceTab.PENDENCIAS) {
                        FloatingActionButton(onClick = { financeViewModel.onEvent(FinanceUiEvent.OpenAddDialog) }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(id = R.string.acao_adicionar)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (state.selectedTab) {
                    FinanceTab.BALANCO -> BalanceScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        currentMonthBalance = state.currentMonthBalance,
                        previousMonthBalance = state.previousMonthBalance,
                        totalSpentCents = state.currentMonthTotalSpentCents,
                        salaryCents = state.currentMonthSalaryCents,
                        nonEssentialExpenseCents = state.currentMonthNonEssentialExpenseCents,
                        investmentCents = state.currentMonthInvestmentCents
                    )

                    FinanceTab.GASTOS -> ExpensesScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        entries = state.entriesForSelectedMonth,
                        recurringTemplates = state.recurringTemplates,
                        selectedMonth = state.selectedEntriesMonth,
                        availableMonths = state.availableMonths,
                        onSelectMonth = { month ->
                            financeViewModel.onEvent(FinanceUiEvent.SelectEntriesMonth(month))
                        },
                        onGenerateMonth = { month ->
                            financeViewModel.onEvent(FinanceUiEvent.GenerateEntriesForMonth(month))
                        },
                        onApplySuggestedRecurringTemplateAmounts = {
                            financeViewModel.onEvent(FinanceUiEvent.ApplySuggestedRecurringTemplateAmounts)
                        },
                        onAddRecurringTemplate = {
                            financeViewModel.onEvent(FinanceUiEvent.OpenAddRecurringTemplateDialog)
                        },
                        onEditRecurringTemplate = { template ->
                            financeViewModel.onEvent(FinanceUiEvent.OpenEditRecurringTemplateDialog(template))
                        },
                        onToggleRecurringTemplateActive = { templateId, isActive ->
                            financeViewModel.onEvent(
                                FinanceUiEvent.UpdateRecurringTemplateActive(templateId, isActive)
                            )
                        },
                        onDeleteRecurringTemplate = { templateId ->
                            financeViewModel.onEvent(FinanceUiEvent.DeleteRecurringTemplate(templateId))
                        },
                        onDeleteEntry = { id ->
                            financeViewModel.onEvent(FinanceUiEvent.DeleteEntry(id))
                        },
                        onToggleStatus = { entryId, status ->
                            financeViewModel.onEvent(FinanceUiEvent.UpdateEntryStatus(entryId, status))
                        },
                        onEditEntry = { entry ->
                            financeViewModel.onEvent(FinanceUiEvent.OpenEditDialog(entry))
                        }
                    )

                    FinanceTab.PENDENCIAS -> PendingScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        pendingEntries = state.pendingEntries,
                        onDeleteEntry = { id ->
                            financeViewModel.onEvent(FinanceUiEvent.DeleteEntry(id))
                        },
                        onMarkAsPaid = { id ->
                            financeViewModel.onEvent(
                                FinanceUiEvent.UpdateEntryStatus(entryId = id, status = EntryStatus.PAID)
                            )
                        },
                        onEditEntry = { entry ->
                            financeViewModel.onEvent(FinanceUiEvent.OpenEditDialog(entry))
                        }
                    )

                    FinanceTab.RELATORIO -> ReportScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        report = state.monthlyReport,
                        selectedMonth = state.selectedReportMonth,
                        availableMonths = state.availableMonths,
                        onSelectMonth = { month ->
                            financeViewModel.onEvent(FinanceUiEvent.SelectReportMonth(month))
                        },
                        onExportCsv = {
                            exportCsvLauncher.launch("relatorio-${state.selectedReportMonth}.csv")
                        },
                        onExportPdf = {
                            exportPdfLauncher.launch("relatorio-${state.selectedReportMonth}.pdf")
                        }
                    )
                }
            }

            if (state.showEntryDialog) {
                EntryDialog(
                    form = state.form,
                    isEditing = state.isEditingEntry,
                    availableMonths = state.availableMonths,
                    onDismiss = { financeViewModel.onEvent(FinanceUiEvent.CloseEntryDialog) },
                    onSave = { financeViewModel.onEvent(FinanceUiEvent.SaveEntry) },
                    onDescriptionChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeDescription(value))
                    },
                    onCategoryChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeCategory(value))
                    },
                    onAmountChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeAmount(value))
                    },
                    onTypeChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeType(value))
                    },
                    onStatusChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeStatus(value))
                    },
                    onReferenceMonthChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeReferenceMonth(value))
                    },
                    onCounterpartyChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeCounterparty(value))
                    }
                )
            }

            if (state.showRecurringTemplateDialog) {
                RecurringTemplateDialog(
                    form = state.recurringTemplateForm,
                    isEditing = state.isEditingRecurringTemplate,
                    onDismiss = { financeViewModel.onEvent(FinanceUiEvent.CloseRecurringTemplateDialog) },
                    onSave = { financeViewModel.onEvent(FinanceUiEvent.SaveRecurringTemplate) },
                    onDescriptionChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateDescription(value))
                    },
                    onCategoryChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateCategory(value))
                    },
                    onAmountChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateAmount(value))
                    },
                    onTypeChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateType(value))
                    },
                    onStatusChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateStatus(value))
                    },
                    onCounterpartyChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateCounterparty(value))
                    },
                    onActiveChange = { value ->
                        financeViewModel.onEvent(FinanceUiEvent.ChangeRecurringTemplateActive(value))
                    }
                )
            }
        }
    }
}

@Composable
private fun BalanceScreen(
    modifier: Modifier,
    currentMonthBalance: MonthBalance,
    previousMonthBalance: MonthBalance,
    totalSpentCents: Long,
    salaryCents: Long,
    nonEssentialExpenseCents: Long,
    investmentCents: Long
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MonthBalanceCard(
                title = "Mês atual: ${formatMonthYear(currentMonthBalance.month)}",
                balance = currentMonthBalance
            )
        }

        item {
            MonthBalanceCard(
                title = "Mês passado: ${formatMonthYear(previousMonthBalance.month)}",
                balance = previousMonthBalance
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Visão rápida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Saldo atual: ${formatCurrencyFromCents(currentMonthBalance.netBalanceCents)}")
                    Text(text = "Saldo mês passado: ${formatCurrencyFromCents(previousMonthBalance.netBalanceCents)}")
                    Text(
                        text = "Diferença: ${formatCurrencyFromCents(currentMonthBalance.netBalanceCents - previousMonthBalance.netBalanceCents)}"
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.titulo_descritivo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.descritivo_total_gasto),
                        value = "- ${formatCurrencyFromCents(totalSpentCents)}",
                        valueColor = ExpenseRed
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.descritivo_salario),
                        value = formatCurrencyFromCents(salaryCents),
                        valueColor = Color(0xFF2962FF)
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.descritivo_despesa_nao_essencial),
                        value = "- ${formatCurrencyFromCents(nonEssentialExpenseCents)}",
                        valueColor = Color(0xFF2962FF)
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.descritivo_investimento),
                        value = formatCurrencyFromCents(investmentCents),
                        valueColor = Color(0xFF2962FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthBalanceCard(
    title: String,
    balance: MonthBalance
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SummaryRow(
                label = stringResource(id = R.string.resumo_receitas),
                value = formatCurrencyFromCents(balance.totalIncomeCents),
                valueColor = IncomeGreen
            )
            SummaryRow(
                label = stringResource(id = R.string.resumo_gastos),
                value = formatCurrencyFromCents(balance.totalExpenseCents),
                valueColor = ExpenseRed
            )
            HorizontalDivider()
            SummaryRow(
                label = stringResource(id = R.string.resumo_saldo),
                value = formatCurrencyFromCents(balance.netBalanceCents),
                valueColor = if (balance.netBalanceCents >= 0) IncomeGreen else ExpenseRed,
                bold = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExpensesScreen(
    modifier: Modifier,
    entries: List<LedgerEntry>,
    recurringTemplates: List<RecurringEntryTemplate>,
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    onSelectMonth: (YearMonth) -> Unit,
    onGenerateMonth: (YearMonth) -> Unit,
    onApplySuggestedRecurringTemplateAmounts: () -> Unit,
    onAddRecurringTemplate: () -> Unit,
    onEditRecurringTemplate: (RecurringEntryTemplate) -> Unit,
    onToggleRecurringTemplateActive: (Long, Boolean) -> Unit,
    onDeleteRecurringTemplate: (Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onToggleStatus: (Long, EntryStatus) -> Unit,
    onEditEntry: (LedgerEntry) -> Unit
) {
    Column(modifier = modifier) {
        MonthSelector(
            availableMonths = availableMonths,
            selectedMonth = selectedMonth,
            onSelectMonth = onSelectMonth,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        RecurringTemplatesCard(
            templates = recurringTemplates,
            selectedMonth = selectedMonth,
            onGenerateMonth = onGenerateMonth,
            onApplySuggestedRecurringTemplateAmounts = onApplySuggestedRecurringTemplateAmounts,
            onAddRecurringTemplate = onAddRecurringTemplate,
            onEditRecurringTemplate = onEditRecurringTemplate,
            onToggleRecurringTemplateActive = onToggleRecurringTemplateActive,
            onDeleteRecurringTemplate = onDeleteRecurringTemplate,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        if (entries.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(entries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    onDeleteEntry = onDeleteEntry,
                    onToggleStatus = onToggleStatus,
                    onEditEntry = onEditEntry
                )
            }
        }
    }
}

@Composable
private fun RecurringTemplatesCard(
    templates: List<RecurringEntryTemplate>,
    selectedMonth: YearMonth,
    onGenerateMonth: (YearMonth) -> Unit,
    onApplySuggestedRecurringTemplateAmounts: () -> Unit,
    onAddRecurringTemplate: () -> Unit,
    onEditRecurringTemplate: (RecurringEntryTemplate) -> Unit,
    onToggleRecurringTemplateActive: (Long, Boolean) -> Unit,
    onDeleteRecurringTemplate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.titulo_base_recorrente),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.subtitulo_base_recorrente),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onGenerateMonth(selectedMonth) }) {
                    Text(text = stringResource(id = R.string.acao_gerar_mes))
                }
                TextButton(onClick = onAddRecurringTemplate) {
                    Text(text = stringResource(id = R.string.acao_adicionar_base))
                }
            }

            TextButton(onClick = onApplySuggestedRecurringTemplateAmounts) {
                Text(text = stringResource(id = R.string.acao_preencher_valores_base))
            }
            Text(
                text = stringResource(id = R.string.subtitulo_valores_base),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (templates.isEmpty()) {
                Text(text = stringResource(id = R.string.base_vazia))
                return@Column
            }

            templates.forEach { template ->
                RecurringTemplateRow(
                    template = template,
                    onEdit = { onEditRecurringTemplate(template) },
                    onToggleActive = { onToggleRecurringTemplateActive(template.id, !template.isActive) },
                    onDelete = { onDeleteRecurringTemplate(template.id) }
                )
            }
        }
    }
}

@Composable
private fun RecurringTemplateRow(
    template: RecurringEntryTemplate,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buildString {
                        append(FinanceCategories.toDisplayLabel(template.category))
                        append(" • ")
                        append(
                            if (template.amountCents != null) {
                                formatCurrencyFromCents(template.amountCents)
                            } else {
                                stringResource(id = R.string.base_sem_valor)
                            }
                        )
                        if (!template.counterparty.isNullOrBlank()) {
                            append(" • ")
                            append(template.counterparty)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = onToggleActive,
                label = {
                    Text(
                        text = if (template.isActive) {
                            stringResource(id = R.string.acao_pausar_base)
                        } else {
                            stringResource(id = R.string.acao_reativar_base)
                        }
                    )
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.acao_editar)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.acao_excluir)
                )
            }
        }

        HorizontalDivider()
    }
}

@Composable
private fun PendingScreen(
    modifier: Modifier,
    pendingEntries: List<LedgerEntry>,
    onDeleteEntry: (Long) -> Unit,
    onMarkAsPaid: (Long) -> Unit,
    onEditEntry: (LedgerEntry) -> Unit
) {
    if (pendingEntries.isEmpty()) {
        EmptyState(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pendingEntries, key = { it.id }) { entry ->
            EntryCard(
                entry = entry,
                onDeleteEntry = onDeleteEntry,
                onToggleStatus = { id, _ -> onMarkAsPaid(id) },
                onEditEntry = onEditEntry
            )
        }
    }
}

@Composable
private fun ReportScreen(
    modifier: Modifier,
    report: MonthlyReport,
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    onSelectMonth: (YearMonth) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MonthSelector(
                availableMonths = availableMonths,
                selectedMonth = selectedMonth,
                onSelectMonth = onSelectMonth
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportCsv) {
                    Text(text = stringResource(id = R.string.acao_exportar_csv))
                }
                Button(onClick = onExportPdf) {
                    Text(text = stringResource(id = R.string.acao_exportar_pdf))
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Resumo de ${formatMonthYear(report.month)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.resumo_receitas),
                        value = formatCurrencyFromCents(report.totalIncomeCents),
                        valueColor = IncomeGreen
                    )
                    SummaryRow(
                        label = stringResource(id = R.string.resumo_gastos),
                        value = formatCurrencyFromCents(report.totalExpenseCents),
                        valueColor = ExpenseRed
                    )
                    HorizontalDivider()
                    SummaryRow(
                        label = stringResource(id = R.string.resumo_saldo),
                        value = formatCurrencyFromCents(report.netBalanceCents),
                        valueColor = if (report.netBalanceCents >= 0) IncomeGreen else ExpenseRed,
                        bold = true
                    )
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.titulo_gastos_por_categoria),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (report.byCategory.isEmpty()) {
                        Text(text = stringResource(id = R.string.sem_dados))
                    } else {
                        report.byCategory.forEach { category ->
                            SummaryRow(
                                label = FinanceCategories.toDisplayLabel(category.category),
                                value = formatCurrencyFromCents(category.totalCents),
                                valueColor = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.titulo_top_gastos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (report.topExpenses.isEmpty()) {
                        Text(text = stringResource(id = R.string.sem_dados))
                    } else {
                        report.topExpenses.forEach { expense ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = FinanceCategories.toDisplayLabel(expense.category),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrencyFromCents(expense.amountCents),
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun EntryCard(
    entry: LedgerEntry,
    onDeleteEntry: (Long) -> Unit,
    onToggleStatus: (Long, EntryStatus) -> Unit,
    onEditEntry: (LedgerEntry) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            append(FinanceCategories.toDisplayLabel(entry.category))
                            append(" • ")
                            append(formatMonthYear(entry.referenceMonth))
                            if (!entry.counterparty.isNullOrBlank()) {
                                append(" • ")
                                append(entry.counterparty)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val amountColor = when {
                    entry.type == EntryType.INCOME -> IncomeGreen
                    entry.status == EntryStatus.PENDING -> PendingOrange
                    else -> ExpenseRed
                }
                Text(
                    text = formatSignedCurrencyFromCents(
                        cents = entry.amountCents,
                        positive = entry.type == EntryType.INCOME
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.status == EntryStatus.PENDING) {
                    AssistChip(
                        onClick = { onToggleStatus(entry.id, EntryStatus.PAID) },
                        label = { Text(text = stringResource(id = R.string.acao_marcar_pago)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                } else {
                    AssistChip(
                        onClick = { onToggleStatus(entry.id, EntryStatus.PENDING) },
                        label = { Text(text = stringResource(id = R.string.acao_marcar_pendente)) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onEditEntry(entry) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.acao_editar)
                    )
                }

                IconButton(onClick = { onDeleteEntry(entry.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.acao_excluir)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onSelectMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableMonths, key = { it.toString() }) { month ->
            FilterChip(
                selected = selectedMonth == month,
                onClick = { onSelectMonth(month) },
                label = { Text(text = formatMonthYear(month)) }
            )
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it }) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(text = FinanceCategories.toDisplayLabel(category)) }
            )
        }
    }
}

@Composable
private fun EntryDialog(
    form: EntryFormState,
    isEditing: Boolean,
    availableMonths: List<YearMonth>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (EntryType) -> Unit,
    onStatusChange: (EntryStatus) -> Unit,
    onReferenceMonthChange: (YearMonth) -> Unit,
    onCounterpartyChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) {
                    stringResource(id = R.string.titulo_editar_lancamento)
                } else {
                    stringResource(id = R.string.titulo_adicionar_lancamento)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = form.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_descricao)) }
                )

                OutlinedTextField(
                    value = form.amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_valor)) },
                    placeholder = { Text(text = "700,00") }
                )

                OutlinedTextField(
                    value = form.counterparty,
                    onValueChange = onCounterpartyChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_responsavel)) }
                )

                Text(text = stringResource(id = R.string.label_tipo), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.type == EntryType.EXPENSE,
                        onClick = { onTypeChange(EntryType.EXPENSE) },
                        label = { Text(text = stringResource(id = R.string.tipo_gasto)) }
                    )
                    FilterChip(
                        selected = form.type == EntryType.INCOME,
                        onClick = { onTypeChange(EntryType.INCOME) },
                        label = { Text(text = stringResource(id = R.string.tipo_receita)) }
                    )
                }

                Text(text = stringResource(id = R.string.label_categoria), style = MaterialTheme.typography.labelLarge)
                CategorySelector(
                    selectedCategory = form.category,
                    categories = FinanceCategories.optionsForType(form.type),
                    onSelectCategory = onCategoryChange
                )

                Text(text = stringResource(id = R.string.label_status), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.status == EntryStatus.PAID,
                        onClick = { onStatusChange(EntryStatus.PAID) },
                        label = { Text(text = stringResource(id = R.string.status_pago)) }
                    )
                    FilterChip(
                        selected = form.status == EntryStatus.PENDING,
                        onClick = { onStatusChange(EntryStatus.PENDING) },
                        label = { Text(text = stringResource(id = R.string.status_pendente)) }
                    )
                }

                Text(text = stringResource(id = R.string.label_referencia), style = MaterialTheme.typography.labelLarge)
                MonthSelector(
                    availableMonths = availableMonths,
                    selectedMonth = form.referenceMonth,
                    onSelectMonth = onReferenceMonthChange
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(id = R.string.acao_salvar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.acao_cancelar))
            }
        }
    )
}

@Composable
private fun RecurringTemplateDialog(
    form: RecurringTemplateFormState,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (EntryType) -> Unit,
    onStatusChange: (EntryStatus) -> Unit,
    onCounterpartyChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) {
                    stringResource(id = R.string.titulo_editar_base)
                } else {
                    stringResource(id = R.string.titulo_adicionar_base)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = form.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_descricao)) }
                )

                OutlinedTextField(
                    value = form.amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_valor)) },
                    placeholder = { Text(text = "700,00") }
                )

                OutlinedTextField(
                    value = form.counterparty,
                    onValueChange = onCounterpartyChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.label_responsavel)) }
                )

                Text(text = stringResource(id = R.string.label_tipo), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.type == EntryType.EXPENSE,
                        onClick = { onTypeChange(EntryType.EXPENSE) },
                        label = { Text(text = stringResource(id = R.string.tipo_gasto)) }
                    )
                    FilterChip(
                        selected = form.type == EntryType.INCOME,
                        onClick = { onTypeChange(EntryType.INCOME) },
                        label = { Text(text = stringResource(id = R.string.tipo_receita)) }
                    )
                }

                Text(text = stringResource(id = R.string.label_categoria), style = MaterialTheme.typography.labelLarge)
                CategorySelector(
                    selectedCategory = form.category,
                    categories = FinanceCategories.optionsForType(form.type),
                    onSelectCategory = onCategoryChange
                )

                Text(text = stringResource(id = R.string.label_status), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.status == EntryStatus.PAID,
                        onClick = { onStatusChange(EntryStatus.PAID) },
                        label = { Text(text = stringResource(id = R.string.status_pago)) }
                    )
                    FilterChip(
                        selected = form.status == EntryStatus.PENDING,
                        onClick = { onStatusChange(EntryStatus.PENDING) },
                        label = { Text(text = stringResource(id = R.string.status_pendente)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.label_base_ativa),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Switch(checked = form.isActive, onCheckedChange = onActiveChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(id = R.string.acao_salvar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.acao_cancelar))
            }
        }
    )
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(id = R.string.sem_dados),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
