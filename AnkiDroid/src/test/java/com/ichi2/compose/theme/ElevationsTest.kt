// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.compose.theme

import androidx.compose.ui.unit.dp
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the [Elevations] scale: the standard Material 0/1/3/6/8/12dp elevation levels.
 */
class ElevationsTest {
    private val elevations = Elevations()

    @Test
    fun `levels follow the standard Material elevation steps`() {
        assertEquals(0.dp, elevations.level0)
        assertEquals(1.dp, elevations.level1)
        assertEquals(3.dp, elevations.level2)
        assertEquals(6.dp, elevations.level3)
        assertEquals(8.dp, elevations.level4)
        assertEquals(12.dp, elevations.level5)
    }

    @Test
    fun `scale increases monotonically`() {
        val scale =
            listOf(
                elevations.level0,
                elevations.level1,
                elevations.level2,
                elevations.level3,
                elevations.level4,
                elevations.level5,
            )
        scale.zipWithNext { smaller, larger ->
            assertTrue(smaller < larger, "$smaller should be < $larger")
        }
    }

    @Test
    fun `copy overrides a single token and leaves the rest`() {
        val custom = elevations.copy(level1 = 2.dp)
        assertEquals(2.dp, custom.level1)
        assertEquals(3.dp, custom.level2, "non-overridden tokens are unchanged")
    }
}
