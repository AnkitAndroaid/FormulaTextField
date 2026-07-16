package com.androaid.formulatextfield

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class FormulaTextFieldUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMathKeypadVisibilityAfterCalculation() {
        var textState by mutableStateOf("")
        var lastResult by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { 
                    lastResult = it
                    textState = it 
                },
                label = "Test Label"
            )
        }

        // 1. Initially, MathKeypad should not be visible
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()

        // 2. Click the = button to enter formula mode
        composeTestRule.onNodeWithTag("FormulaButton").performClick()

        // 3. MathKeypad should be visible
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()

        // 4. Enter a formula: 10 + 20
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("0").performClick()

        // 5. Click Calculate
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // 6. Verify result is 30 and MathKeypad is gone
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("30")
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()

        // 7. Click the = button again
        composeTestRule.onNodeWithTag("FormulaButton").performClick()

        // 8. MathKeypad should be visible again
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()

        // 9. Repeat: Enter another formula: +10 (total =30+10)
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // 10. Verify result is 40
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("40")
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()
    }

    @Test
    fun testManualFormulaActivation() {
        var textState by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { textState = it },
                label = "Input"
            )
        }

        // Initially gone
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()

        // Type "=" manually
        composeTestRule.onNodeWithTag("FormulaTextField").performTextInput("=")
        
        // Keypad should appear
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()
    }

    @Test
    fun testOperatorPrecedence() {
        var textState by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { textState = it }
            )
        }

        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        
        // Enter 2 + 3 * 4
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("*").performClick()
        composeTestRule.onNodeWithText("4").performClick()

        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // Should be 14, not 20
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("14")
    }

    @Test
    fun testErrorHandling() {
        var textState by mutableStateOf("")
        var lastResult by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { 
                    lastResult = it
                    textState = it
                }
            )
        }

        // 1. Division by Zero
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("/").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()
        
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("Division by zero", substring = true)

        // 2. Invalid Syntax
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("Unexpected character", substring = true)
    }

    @Test
    fun testBackspaceFunctionality() {
        var textState by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { textState = it }
            )
        }

        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()

        // Backspace twice
        composeTestRule.onNodeWithContentDescription("Backspace").performClick()
        composeTestRule.onNodeWithContentDescription("Backspace").performClick()

        // Check text
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("=1")
    }

    @Test
    fun testFloatingPointPrecision() {
        var textState by mutableStateOf("")

        composeTestRule.setContent {
            FormulaTextField(
                value = textState,
                onValueChange = { textState = it },
                onEvaluateResult = { textState = it }
            )
        }

        // Test 1/3
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("/").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // Check result contains 0.333
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("0.333", substring = true)

        // Test 0.1 + 0.2
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        // We need to clear previous result first, or it might be appended? 
        // Clicking FormulaButton currently PREPENDS '=' if not present.
        // If it already had "0.333...", clicking FormulaButton might result in "=0.333..."
        // I should probably clear it.
        composeTestRule.onNodeWithTag("FormulaTextField").performTextReplacement("")
        
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText(".").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText(".").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // Check result contains 0.3
        composeTestRule.onNodeWithTag("FormulaTextField").assertTextContains("0.3", substring = true)
    }

    @Test
    fun testFocusManagement() {
        var textState by mutableStateOf("")

        composeTestRule.setContent {
            Column {
                FormulaTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    onEvaluateResult = { textState = it },
                    label = "Input"
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.testTag("OtherField")
                )
            }
        }

        // 1. Focus the field and enter formula mode
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()

        // 2. Click "OtherField" to take focus
        composeTestRule.onNodeWithTag("OtherField").performClick()

        // 3. Keypad should disappear
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()
    }
}
