package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.PdfCraftDatabase
import com.example.ui.components.AdaptiveLayout
import com.example.ui.theme.PdfCraftTheme
import com.example.viewmodel.PdfCraftViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PdfCraftDatabase.getDatabase(applicationContext)
        val viewModel = PdfCraftViewModel(database)

        setContent {
            PdfCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdaptiveLayout(viewModel = viewModel)
                }
            }
        }
    }
}
