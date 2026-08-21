/*
 * Copyright (c) 2025 Brayan Oliveira <69634269+brayandso@users.noreply.github.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.preferences

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.RobolectricTest
import com.ichi2.testutils.HamcrestUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlsSettingsFragmentTest : RobolectricTest() {
    @Test
    fun `XML keys match the Enum keys`() {
        for (screen in ControlPreferenceScreen.entries) {
            val xmlKeys =
                PreferenceTestUtils.getKeysFromXml(targetContext, screen.xmlRes, excludeCategories = true).toMutableList().apply {
                    remove("binding_whiteboard_UNDO")
                    remove("binding_whiteboard_REDO")
                    remove("binding_whiteboard_CLEAR")
                    remove("binding_whiteboard_TOGGLE_ERASER")
                    // These commands only exist in the legacy Reviewer, not in ViewerAction
                    remove("binding_SAVE_VOICE")
                    remove("binding_TOGGLE_ERASER")
                    remove("binding_CLEAR_WHITEBOARD")
                    remove("binding_CHANGE_WHITEBOARD_PEN_COLOR")
                }
            val enumKeys =
                screen.getActions().map { it.preferenceKey }.toMutableList().apply {
                    // Menu-only entries: shown as a menu/submenu item, not individually bindable
                    removeAll(
                        listOf(
                            "binding_FLAG_MENU",
                            "binding_BURY_MENU",
                            "binding_SUSPEND_MENU",
                            "binding_DECK_OPTIONS",
                            "binding_RESET_PROGRESS",
                            "binding_FLAG_RED",
                            "binding_FLAG_ORANGE",
                            "binding_FLAG_GREEN",
                            "binding_FLAG_BLUE",
                            "binding_FLAG_PINK",
                            "binding_FLAG_TURQUOISE",
                            "binding_FLAG_PURPLE",
                        ),
                    )
                }

            assertThat(xmlKeys, HamcrestUtils.containsInAnyOrder(enumKeys))
        }
    }
}
