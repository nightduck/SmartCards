// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Pins [SmartCardsTypography]: every role matches the stock Material3 scale except for
 * a uniform +2sp of line-height.
 */
class TypographyTest {
    private val stock = Typography()

    @Test
    fun `every role adds exactly 2sp of line-height over the Material3 default`() {
        val roles =
            listOf(
                SmartCardsTypography.displayLarge to stock.displayLarge,
                SmartCardsTypography.displayMedium to stock.displayMedium,
                SmartCardsTypography.displaySmall to stock.displaySmall,
                SmartCardsTypography.headlineLarge to stock.headlineLarge,
                SmartCardsTypography.headlineMedium to stock.headlineMedium,
                SmartCardsTypography.headlineSmall to stock.headlineSmall,
                SmartCardsTypography.titleLarge to stock.titleLarge,
                SmartCardsTypography.titleMedium to stock.titleMedium,
                SmartCardsTypography.titleSmall to stock.titleSmall,
                SmartCardsTypography.bodyLarge to stock.bodyLarge,
                SmartCardsTypography.bodyMedium to stock.bodyMedium,
                SmartCardsTypography.bodySmall to stock.bodySmall,
                SmartCardsTypography.labelLarge to stock.labelLarge,
                SmartCardsTypography.labelMedium to stock.labelMedium,
                SmartCardsTypography.labelSmall to stock.labelSmall,
            )
        roles.forEach { (smartCards, stockStyle) ->
            assertEquals(
                (stockStyle.lineHeight.value + 2f).sp,
                smartCards.lineHeight,
                "expected ${stockStyle.lineHeight} + 2sp",
            )
            // Font size, weight and letter-spacing are untouched.
            assertEquals(stockStyle.fontSize, smartCards.fontSize)
            assertEquals(stockStyle.fontWeight, smartCards.fontWeight)
            assertEquals(stockStyle.letterSpacing, smartCards.letterSpacing)
        }
    }
}
