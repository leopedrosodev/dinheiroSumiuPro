package com.dinheirosumiupro.app.presentation.common

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.dinheirosumiupro.app.domain.model.EntryStatus
import com.dinheirosumiupro.app.domain.model.EntryType
import com.dinheirosumiupro.app.domain.model.LedgerEntry
import com.dinheirosumiupro.app.domain.model.MonthlyReport
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val csvDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

object ReportExporter {
    fun buildMonthlyCsv(report: MonthlyReport, entries: List<LedgerEntry>): String {
        val builder = StringBuilder()
        builder.appendLine("Relatorio;${formatMonthYear(report.month)}")
        builder.appendLine("Receitas;${formatCurrencyFromCents(report.totalIncomeCents)}")
        builder.appendLine("Gastos;${formatCurrencyFromCents(report.totalExpenseCents)}")
        builder.appendLine("Saldo;${formatCurrencyFromCents(report.netBalanceCents)}")
        builder.appendLine()
        builder.appendLine("Descricao;Categoria;Tipo;Status;Mes;Valor;Criado em;Observacao")

        entries.forEach { entry ->
            val createdAt = Instant.ofEpochMilli(entry.createdAtMillis)
                .atZone(ZoneId.systemDefault())
                .format(csvDateFormatter)

            builder.appendLine(
                listOf(
                    escapeCsv(entry.description),
                    escapeCsv(FinanceCategories.toDisplayLabel(entry.category)),
                    if (entry.type == EntryType.INCOME) "Receita" else "Gasto",
                    if (entry.status == EntryStatus.PAID) "Pago" else "Pendente",
                    formatMonthYear(entry.referenceMonth),
                    formatCurrencyFromCents(entry.amountCents),
                    createdAt,
                    escapeCsv(entry.counterparty.orEmpty())
                ).joinToString(";")
            )
        }

        return builder.toString()
    }

    fun buildMonthlyPdf(report: MonthlyReport, entries: List<LedgerEntry>): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        val sectionPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 11f
        }

        var y = 42f
        val x = 36f

        canvas.drawText("Relatório - ${formatMonthYear(report.month)}", x, y, titlePaint)
        y += 28f

        canvas.drawText("Receitas: ${formatCurrencyFromCents(report.totalIncomeCents)}", x, y, textPaint)
        y += 18f
        canvas.drawText("Gastos: ${formatCurrencyFromCents(report.totalExpenseCents)}", x, y, textPaint)
        y += 18f
        canvas.drawText("Saldo: ${formatCurrencyFromCents(report.netBalanceCents)}", x, y, textPaint)
        y += 28f

        canvas.drawText("Gastos por categoria", x, y, sectionPaint)
        y += 18f

        if (report.byCategory.isEmpty()) {
            canvas.drawText("Sem dados", x, y, textPaint)
            y += 18f
        } else {
            report.byCategory.take(10).forEach { category ->
                canvas.drawText(
                    "- ${FinanceCategories.toDisplayLabel(category.category)}: ${formatCurrencyFromCents(category.totalCents)}",
                    x,
                    y,
                    textPaint
                )
                y += 16f
            }
        }

        y += 12f
        canvas.drawText("Lançamentos do mês", x, y, sectionPaint)
        y += 18f

        entries.take(18).forEach { entry ->
            val typeText = if (entry.type == EntryType.INCOME) "R" else "G"
            val statusText = if (entry.status == EntryStatus.PAID) "Pago" else "Pendente"
            val row = "$typeText | ${entry.description.take(36)} | ${formatCurrencyFromCents(entry.amountCents)} | $statusText"
            canvas.drawText(row, x, y, textPaint)
            y += 15f
        }

        if (entries.size > 18) {
            y += 8f
            canvas.drawText("(Mostrando 18 de ${entries.size} lançamentos)", x, y, textPaint)
        }

        document.finishPage(page)

        val output = ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    private fun escapeCsv(input: String): String {
        if (input.contains(';') || input.contains('"') || input.contains('\n')) {
            return '"' + input.replace("\"", "\"\"") + '"'
        }
        return input
    }
}
