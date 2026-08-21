// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * SmartCards corner-radius scale, read the same way as any other Material3 slot
 * (`MaterialTheme.shapes.medium`).
 *
 * Rounder than the stock Material3 scale (4/8/12/16/28dp) on purpose: this is a calm,
 * de-cluttered language-learning app, not a dense power-user tool, and softer corners read
 * as friendlier at the sizes AnkiDroid's cards, sheets and buttons actually use.
 *
 * There is no XML/View equivalent to bridge this from -- View-based AnkiDroid screens don't
 * share a single corner-radius scale today (each dialog/card sets its own `dialog_corner_radius`,
 * `cardCornerRadius`, etc. -- see dimens.xml and styles.xml) -- so, unlike the color palette,
 * this doesn't need a `R.styleable.ComposeTheme`-style bridge. It is a new scale for the
 * upcoming Compose screens (the capture sheet and beyond) to standardize on.
 */
private val CornerExtraSmall = 6.dp
private val CornerSmall = 10.dp
private val CornerMedium = 16.dp
private val CornerLarge = 24.dp
private val CornerExtraLarge = 32.dp

val SmartCardsShapes =
    Shapes(
        extraSmall = RoundedCornerShape(CornerExtraSmall),
        small = RoundedCornerShape(CornerSmall),
        medium = RoundedCornerShape(CornerMedium),
        large = RoundedCornerShape(CornerLarge),
        extraLarge = RoundedCornerShape(CornerExtraLarge),
    )
