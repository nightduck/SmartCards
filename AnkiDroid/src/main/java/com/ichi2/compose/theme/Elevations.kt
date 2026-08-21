// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 Ashish Yadav <mailtoashish693@gmail.com>

package com.ichi2.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation scale for Compose screens, read the same way as [Dimensions]
 * (`MaterialTheme.elevations.level1`). Material3 ships no elevation tokens of its own --
 * the standard `Surface`/`Card` still take a literal `Dp`, they just also apply a tonal
 * surface-color shift as elevation increases (already handled by the `colorSurfaceContainer*`
 * slots bridged in [toMaterial3ColorScheme]) -- so this fills the same kind of gap [Dimensions]
 * fills for spacing.
 *
 * The steps mirror Material Design's standard 0/1/3/6/8/12dp elevation levels -- the same
 * family the pre-existing `?attr/studyScreenElevation` XML value (0.8dp, roughly level1) is
 * already drawing from, just formalized as a full scale for Compose to standardize on.
 */
@Immutable
data class Elevations(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
)

/**
 * Provides the active [Elevations] down the Compose tree. Prefer reading via
 * [MaterialTheme.elevations]; use this directly only to override tokens in a
 * `CompositionLocalProvider` (previews, tests).
 */
val LocalElevations = staticCompositionLocalOf { Elevations() }

/**
 * Access the elevation scale the same way as [MaterialTheme.colorScheme] or
 * [MaterialTheme.typography].
 */
val MaterialTheme.elevations: Elevations
    @Composable
    @ReadOnlyComposable
    get() = LocalElevations.current
