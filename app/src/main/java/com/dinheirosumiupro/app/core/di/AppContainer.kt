package com.dinheirosumiupro.app.core.di

import android.content.Context
import androidx.room.Room
import com.dinheirosumiupro.app.data.local.FinanceDatabase
import com.dinheirosumiupro.app.data.repository.FinanceRepository
import com.dinheirosumiupro.app.domain.usecase.BuildMonthlyReportUseCase
import com.dinheirosumiupro.app.domain.usecase.CalculateMonthBalanceUseCase

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database = Room.databaseBuilder(
        appContext,
        FinanceDatabase::class.java,
        "dinheiro_sumiu_pro.db"
    ).build()

    val financeRepository = FinanceRepository(database.ledgerDao())
    val calculateMonthBalanceUseCase = CalculateMonthBalanceUseCase()
    val buildMonthlyReportUseCase = BuildMonthlyReportUseCase()
}
