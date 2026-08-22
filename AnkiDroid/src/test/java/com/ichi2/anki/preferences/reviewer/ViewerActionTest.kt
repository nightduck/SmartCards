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
}
