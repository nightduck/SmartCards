// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.preferences.reviewer

import android.content.SharedPreferences
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import com.ichi2.anki.cardviewer.Gesture
import com.ichi2.anki.reviewer.Binding
import com.ichi2.anki.reviewer.CardSide
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class ViewerActionTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        prefs = SPMockBuilder().createSharedPreferences()
    }

    /**
     * #19: tapping the card (in any of the nine tap zones - see [com.ichi2.anki.cardviewer.TapGestureMode])
     * should reveal the answer, replacing the removed `show_answer_button`.
     */
    @Test
    fun `SHOW_ANSWER has a default gesture binding for every single-tap zone`() {
        val bindings = ViewerAction.SHOW_ANSWER.getBindings(prefs)

        val tapGestures =
            listOf(
                Gesture.TAP_TOP_LEFT,
                Gesture.TAP_TOP,
                Gesture.TAP_TOP_RIGHT,
                Gesture.TAP_LEFT,
                Gesture.TAP_CENTER,
                Gesture.TAP_RIGHT,
                Gesture.TAP_BOTTOM_LEFT,
                Gesture.TAP_BOTTOM,
                Gesture.TAP_BOTTOM_RIGHT,
            )

        for (gesture in tapGestures) {
            val binding =
                bindings.firstOrNull { it.binding == Binding.GestureInput(gesture) }
                    ?: error("No default binding found for $gesture")
            assertTrue(binding.side == CardSide.QUESTION, "Expected $gesture to only trigger on the question side")
        }
    }

    @Test
    fun `SHOW_ANSWER has no default binding for DOUBLE_TAP`() {
        val bindings = ViewerAction.SHOW_ANSWER.getBindings(prefs)

        assertTrue(bindings.none { it.binding == Binding.GestureInput(Gesture.DOUBLE_TAP) })
    }

    @Test
    fun `SHOW_ANSWER keyboard bindings are unaffected`() {
        val bindings = ViewerAction.SHOW_ANSWER.getBindings(prefs)

        assertTrue(bindings.count { it.isKey } == 3)
    }
}
