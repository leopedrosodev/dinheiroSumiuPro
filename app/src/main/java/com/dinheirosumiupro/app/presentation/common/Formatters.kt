package com.dinheirosumiupro.app.presentation.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val brLocale = Locale("pt", "BR")
private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(brLocale)
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", brLocale)

fun formatCurrencyFromCents(cents: Long): String {
    return currencyFormatter.format(cents / 100.0)
}

fun formatSignedCurrencyFromCents(cents: Long, positive: Boolean): String {
    val prefix = if (positive) "+ " else "- "
    return prefix + formatCurrencyFromCents(cents)
}

fun parseCurrencyToCents(input: String): Long? {
    val normalized = input
        .trim()
        .replace("R$", "", ignoreCase = true)
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")

    if (normalized.isBlank()) return null

    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value <= BigDecimal.ZERO) return null

    return value
        .multiply(BigDecimal(100))
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

fun formatMonthYear(month: YearMonth): String {
    val formatted = month.format(monthFormatter)
    return formatted.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase(brLocale)
        } else {
            char.toString()
        }
    }
}

fun formatCentsToInput(cents: Long): String {
    val value = cents / 100.0
    return String.format(brLocale, "%.2f", value).replace(".", ",")
}
