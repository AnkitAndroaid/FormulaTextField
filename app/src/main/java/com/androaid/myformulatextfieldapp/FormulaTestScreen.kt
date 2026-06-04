package com.androaid.myformulatextfieldapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androaid.formulatextfield.FormulaTextField

@Composable
fun FormulaTestScreen(innerPadding: PaddingValues) {
    var textState by rememberSaveable { mutableStateOf("(10 + 5) * 3") }

    Column(modifier = Modifier.padding(24.dp)) {
        FormulaTextField(
            value = textState,
            onValueChange = { textState = it },
            onEvaluateResult = { computedValue ->
                // Overwrites the input field with the answer when Enter is pressed!
                textState = computedValue 
            },
            label = "Type & Press Enter"
        )
        
        Text(
            text = "Press the Enter key on your hardware keyboard or tap 'Done' on the soft keyboard to calculate.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}