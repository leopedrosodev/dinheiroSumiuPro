package com.dinheirosumiupro.app.presentation.common

import com.dinheirosumiupro.app.domain.model.EntryType

object FinanceCategories {
    const val SALARIO = "SALARIO"
    const val DESPESA_NAO_ESSENCIAL = "DESPESA_NAO_ESSENCIAL"
    const val INVESTIMENTO = "INVESTIMENTO"
    const val MORADIA = "MORADIA"
    const val CONTAS_FIXAS = "CONTAS_FIXAS"
    const val ALIMENTACAO = "ALIMENTACAO"
    const val TRANSPORTE = "TRANSPORTE"
    const val SAUDE = "SAUDE"
    const val OUTROS = "OUTROS"

    private val incomeCategories = listOf(
        SALARIO,
        INVESTIMENTO,
        OUTROS
    )

    private val expenseCategories = listOf(
        MORADIA,
        CONTAS_FIXAS,
        ALIMENTACAO,
        TRANSPORTE,
        SAUDE,
        DESPESA_NAO_ESSENCIAL,
        INVESTIMENTO,
        OUTROS
    )

    fun optionsForType(type: EntryType): List<String> {
        return when (type) {
            EntryType.INCOME -> incomeCategories
            EntryType.EXPENSE -> expenseCategories
        }
    }

    fun toDisplayLabel(category: String): String {
        return when (category) {
            SALARIO -> "Salário"
            DESPESA_NAO_ESSENCIAL -> "Despesa não essencial"
            INVESTIMENTO -> "Investimento"
            MORADIA -> "Moradia"
            CONTAS_FIXAS -> "Contas fixas"
            ALIMENTACAO -> "Alimentação"
            TRANSPORTE -> "Transporte"
            SAUDE -> "Saúde"
            OUTROS -> "Outros"
            else -> category.ifBlank { "Outros" }
        }
    }
}
