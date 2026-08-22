// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 Oren <virtualoren@gmail.com>

package com.ichi2.anki.previewer

import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.R
import com.ichi2.anki.RobolectricTest
import com.ichi2.anki.common.android.Animations
import com.ichi2.compose.theme.SmartCardsCorners
import com.ichi2.utils.toRGBHex
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pins the Phase 0 tokens (issue #17) that [stdHtml] hands to a card's CSS.
 *
 * A card renders in a WebView, so `ankidroid.css` can't read a theme attribute itself; the
 * dictionary explainer panel (issue #23) is drawn from these custom properties.
 */
@RunWith(AndroidJUnit4::class)
class PreviewerHelpersTest : RobolectricTest() {
    @Test
    fun `explainer accent comes from the theme's secondary colour`() {
        val secondary = com.google.android.material.R.attr.colorSecondary
        val expected = ThemeUtils.getThemeAttrColor(targetContext, secondary).toRGBHex()

        assertThat(stdHtml(targetContext), containsString("--sc-explainer-accent: $expected;"))
    }

    @Test
    fun `explainer radius comes from the SmartCards corner scale`() {
        val expected = SmartCardsCorners.medium.value.toInt()

        assertThat(stdHtml(targetContext), containsString("--sc-explainer-radius: ${expected}px;"))
    }

    @Test
    fun `motion duration is left to the stylesheet while animations are enabled`() {
        assertThat(Animations.areAnimationsEnabled(targetContext), equalTo(true))

        assertThat(stdHtml(targetContext), not(containsString("--sc-motion-duration")))
    }

    @Test
    fun `motion duration is zeroed when animations are disabled`() {
        // 'Safe display mode' is the app's own 'remove animations' setting.
        getPreferences().edit { putBoolean(getResourceString(R.string.safe_display_key), true) }
        assertThat(Animations.areAnimationsEnabled(targetContext), equalTo(false))

        assertThat(stdHtml(targetContext), containsString("--sc-motion-duration: 0ms;"))
    }
}
