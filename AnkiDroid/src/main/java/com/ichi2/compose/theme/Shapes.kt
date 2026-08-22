// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The raw corner-radius values behind [SmartCardsShapes].
 *
 * [SmartCardsShapes] is the way to read these from Compose. This object exists for the surfaces
 * that can't take a Compose `Shape` at all: the reviewer renders cards in a WebView, and
 * `com.ichi2.anki.previewer.stdHtml` hands these values to its CSS so a panel drawn inside a card
 * has the same corners as one drawn by the app around it.
 */
object SmartCardsCorners {
    val extraSmall: Dp = 6.dp
    val small: Dp = 10.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
}

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
 * this doesn't need a `R.styleable.ComposeTheme`-style bridge. It is the scale for the upcoming
 * Compose screens (the capture sheet and beyond) to standardize on, and, via
 * [SmartCardsCorners], for anything the app draws outside the View/Compose world.
 */
val SmartCardsShapes =
    Shapes(
        extraSmall = RoundedCornerShape(SmartCardsCorners.extraSmall),
        small = RoundedCornerShape(SmartCardsCorners.small),
        medium = RoundedCornerShape(SmartCardsCorners.medium),
        large = RoundedCornerShape(SmartCardsCorners.large),
        extraLarge = RoundedCornerShape(SmartCardsCorners.extraLarge),
    )
