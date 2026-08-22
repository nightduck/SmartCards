// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 Oren <virtualoren@gmail.com>

package com.ichi2.anki.preferences.reviewer

import android.content.SharedPreferences
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import com.ichi2.anki.cardviewer.Gesture
import com.ichi2.anki.reviewer.Binding
import com.ichi2.anki.reviewer.CardSide
import com.ichi2.anki.reviewer.ReviewerBinding
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * #20: swipe left/right should rate Hard/Good by default, matching #14's UI overhaul spec.
 */
class ViewerActionTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        prefs = SPMockBuilder().createSharedPreferences()
    }

    private fun List<ReviewerBinding>.hasGesture(
        gesture: Gesture,
        side: CardSide,
    ): Boolean = any { it == ReviewerBinding(binding = Binding.gesture(gesture), side = side) }

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
    fun `swipe left rates Hard by default`() {
        val bindings = ViewerAction.ANSWER_HARD.getBindings(prefs)
        assertTrue(bindings.hasGesture(Gesture.SWIPE_LEFT, CardSide.ANSWER))
    }

    @Test
    fun `swipe right rates Good by default`() {
        val bindings = ViewerAction.ANSWER_GOOD.getBindings(prefs)
        assertTrue(bindings.hasGesture(Gesture.SWIPE_RIGHT, CardSide.ANSWER))
    }

    @Test
    fun `swipe left and right also flip the card on the question side`() {
        val bindings = ViewerAction.SHOW_ANSWER.getBindings(prefs)
        assertTrue(bindings.hasGesture(Gesture.SWIPE_LEFT, CardSide.QUESTION))
        assertTrue(bindings.hasGesture(Gesture.SWIPE_RIGHT, CardSide.QUESTION))
    }

    @Test
    fun `ANSWER_HARD and ANSWER_GOOD swipes don't leak onto the question side`() {
        val hardBindings = ViewerAction.ANSWER_HARD.getBindings(prefs)
        val goodBindings = ViewerAction.ANSWER_GOOD.getBindings(prefs)
        assertTrue(!hardBindings.hasGesture(Gesture.SWIPE_LEFT, CardSide.QUESTION))
        assertTrue(!goodBindings.hasGesture(Gesture.SWIPE_RIGHT, CardSide.QUESTION))
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
