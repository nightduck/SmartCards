// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * SmartCards type scale, read the same way as any other Material3 slot
 * (`MaterialTheme.typography.bodyLarge`).
 *
 * Font sizes, weights and letter-spacing are the stock Material3 scale -- there's no
 * SmartCards brand typeface yet (that lands with the rebrand, issue #38), so there was
 * nothing to justify deviating from the well-tested M3 defaults there. The one deliberate
 * change is two extra sp of line-height on every role: a small, uniform bump for a calmer,
 * less cramped reading rhythm, in line with de-cluttering this from a power-user tool into
 * a language-learning app. Revisit once a brand typeface is chosen.
 */
private const val EXTRA_LINE_HEIGHT_SP = 2f

// TextUnit has no `plus` operator, so the sp value is unpacked and re-wrapped by hand.
private fun TextStyle.withExtraLineHeight(): TextStyle =
    if (lineHeight.isSp) {
        copy(lineHeight = (lineHeight.value + EXTRA_LINE_HEIGHT_SP).sp)
    } else {
        this
    }

private val baseline = Typography()

val SmartCardsTypography =
    Typography(
        displayLarge = baseline.displayLarge.withExtraLineHeight(),
        displayMedium = baseline.displayMedium.withExtraLineHeight(),
        displaySmall = baseline.displaySmall.withExtraLineHeight(),
        headlineLarge = baseline.headlineLarge.withExtraLineHeight(),
        headlineMedium = baseline.headlineMedium.withExtraLineHeight(),
        headlineSmall = baseline.headlineSmall.withExtraLineHeight(),
        titleLarge = baseline.titleLarge.withExtraLineHeight(),
        titleMedium = baseline.titleMedium.withExtraLineHeight(),
        titleSmall = baseline.titleSmall.withExtraLineHeight(),
        bodyLarge = baseline.bodyLarge.withExtraLineHeight(),
        bodyMedium = baseline.bodyMedium.withExtraLineHeight(),
        bodySmall = baseline.bodySmall.withExtraLineHeight(),
        labelLarge = baseline.labelLarge.withExtraLineHeight(),
        labelMedium = baseline.labelMedium.withExtraLineHeight(),
        labelSmall = baseline.labelSmall.withExtraLineHeight(),
    )
