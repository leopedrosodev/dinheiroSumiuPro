package com.dinheirosumiupro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dinheirosumiupro.app.core.di.AppContainer
import com.dinheirosumiupro.app.presentation.FinanceRoot

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FinanceRoot(appContainer = appContainer)
        }
    }
}
