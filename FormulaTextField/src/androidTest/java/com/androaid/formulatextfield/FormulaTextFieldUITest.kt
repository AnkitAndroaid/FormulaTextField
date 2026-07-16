package com.androaid.formulatextfield

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
        composeTestRule.onNodeWithText("30").assertIsDisplayed()
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()

        // 7. Click the = button again
        composeTestRule.onNodeWithTag("FormulaButton").performClick()

        // 8. MathKeypad should be visible again (This is where the bug is reported)
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()

        // 9. Repeat: Enter another formula: +10 (total =30+10)
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithTag("CalculateButton").performClick()

        // 10. Verify result is 40
        composeTestRule.onNodeWithText("40").assertIsDisplayed()
        composeTestRule.onNodeWithTag("MathKeypad").assertDoesNotExist()

        // 11. Click = icon again
        composeTestRule.onNodeWithTag("FormulaButton").performClick()
        composeTestRule.onNodeWithTag("MathKeypad").assertIsDisplayed()
    }
}
