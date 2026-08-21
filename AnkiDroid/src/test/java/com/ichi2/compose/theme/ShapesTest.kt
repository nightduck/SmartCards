// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 Ashish Yadav <mailtoashish693@gmail.com>

package com.ichi2.compose.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the [SmartCardsShapes] corner-radius scale.
 */
class ShapesTest {
    private val density = Density(1f)

    private fun RoundedCornerShape.topStartPx() = topStart.toPx(Size.Zero, density)

    @Test
    fun `corner scale is rounder than the stock Material3 defaults`() {
        // Stock Material3 Shapes(): 4 / 8 / 12 / 16 / 28dp.
        val stock = Shapes()
        val cases =
            listOf(
                SmartCardsShapes.extraSmall as RoundedCornerShape to stock.extraSmall as RoundedCornerShape,
                SmartCardsShapes.small as RoundedCornerShape to stock.small as RoundedCornerShape,
                SmartCardsShapes.medium as RoundedCornerShape to stock.medium as RoundedCornerShape,
                SmartCardsShapes.large as RoundedCornerShape to stock.large as RoundedCornerShape,
            )
        cases.forEach { (smartCards, stockShape) ->
            assertTrue(
                smartCards.topStartPx() > stockShape.topStartPx(),
                "${smartCards.topStartPx()} should be rounder than stock ${stockShape.topStartPx()}",
            )
        }
    }

    @Test
    fun `corner scale increases monotonically`() {
        val corners =
            listOf(
                SmartCardsShapes.extraSmall as RoundedCornerShape,
                SmartCardsShapes.small as RoundedCornerShape,
                SmartCardsShapes.medium as RoundedCornerShape,
                SmartCardsShapes.large as RoundedCornerShape,
                SmartCardsShapes.extraLarge as RoundedCornerShape,
            )
        corners.zipWithNext { smaller, larger ->
            assertTrue(
                smaller.topStartPx() < larger.topStartPx(),
                "${smaller.topStartPx()} should be < ${larger.topStartPx()}",
            )
        }
    }

    @Test
    fun `medium corner matches the documented 16dp token`() {
        val expected = CornerSize(16.dp).toPx(Size.Zero, density)
        assertEquals(expected, (SmartCardsShapes.medium as RoundedCornerShape).topStartPx())
    }
}
