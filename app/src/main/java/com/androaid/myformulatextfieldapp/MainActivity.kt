package com.androaid.myformulatextfieldapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.androaid.myformulatextfieldapp.ui.theme.MyFormulaTextFieldAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFormulaTextFieldAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                FormulaTestScreen(innerPadding)
                }
            }
        }
    }
}

