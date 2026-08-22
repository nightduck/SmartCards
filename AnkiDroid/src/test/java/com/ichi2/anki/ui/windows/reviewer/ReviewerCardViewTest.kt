// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 Oren Bell <virtualoren@gmail.com>

package com.ichi2.anki.ui.windows.reviewer

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import anki.scheduler.CardAnswer.Rating
import com.ichi2.anki.cardviewer.Gesture
import com.ichi2.anki.ui.windows.reviewer.ReviewerCardView.SwipeDirection
import com.ichi2.themes.Themes
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ReviewerCardViewTest {
    private lateinit var cardView: ReviewerCardView
    private var swipedGesture: Gesture? = null

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Themes.setTheme(context)
        cardView =
            ReviewerCardView(context).apply {
                // A child is needed to take the initial touch, like the card's WebView does
                addView(View(context).apply { isClickable = true }, CARD_WIDTH, CARD_HEIGHT)
                onSwipe = { gesture ->
                    swipedGesture = gesture
                    true
                }
            }
        cardView.measure(
            View.MeasureSpec.makeMeasureSpec(CARD_WIDTH, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(CARD_HEIGHT, View.MeasureSpec.EXACTLY),
        )
        cardView.layout(0, 0, CARD_WIDTH, CARD_HEIGHT)
    }

    @Test
    fun `card follows the finger while dragging`() {
        val origin = beginDrag(towardsRight = true)
        dispatch(MotionEvent.ACTION_MOVE, origin + 200f)

        assertEquals(200f, cardView.translationX)
        assertTrue(cardView.rotation > 0f, "the card should tilt towards the drag")
    }

    @Test
    fun `dragging right past the threshold swipes right`() {
        drag(deltaX = CARD_WIDTH * 0.6f)

        assertEquals(Gesture.SWIPE_RIGHT, swipedGesture)
    }

    @Test
    fun `dragging left past the threshold swipes left`() {
        drag(deltaX = -CARD_WIDTH * 0.6f)

        assertEquals(Gesture.SWIPE_LEFT, swipedGesture)
    }

    @Test
    fun `releasing before the threshold does not swipe`() {
        drag(deltaX = CARD_WIDTH * 0.1f)

        assertNull(swipedGesture)
    }

    @Test
    fun `a mostly vertical drag is left to the content`() {
        dispatch(MotionEvent.ACTION_DOWN, START_X)
        dispatch(MotionEvent.ACTION_MOVE, START_X + 60f, START_Y + 200f)
        dispatch(MotionEvent.ACTION_MOVE, START_X + 80f, START_Y + 400f)

        assertEquals(0f, cardView.translationX)
    }

    @Test
    fun `a drag is left to content that scrolls horizontally`() {
        cardView.canContentScrollHorizontally = { true }

        drag(deltaX = CARD_WIDTH * 0.6f)

        assertEquals(0f, cardView.translationX)
        assertNull(swipedGesture)
    }

    @Test
    fun `dragging is ignored while disabled`() {
        cardView.isDragEnabled = false

        drag(deltaX = CARD_WIDTH * 0.6f)

        assertEquals(0f, cardView.translationX)
        assertNull(swipedGesture)
    }

    @Test
    fun `a thrown card ignores drags until its replacement arrives`() {
        cardView.swipeOut(SwipeDirection.LEFT)

        drag(deltaX = CARD_WIDTH * 0.6f)

        assertNull(swipedGesture)
    }

    @Test
    fun `a thrown card accepts drags again once the next card is shown`() {
        cardView.swipeOut(SwipeDirection.LEFT)
        cardView.onNextCardShown()

        drag(deltaX = CARD_WIDTH * 0.6f)

        assertEquals(Gesture.SWIPE_RIGHT, swipedGesture)
    }

    @Test
    fun `harsh ratings swipe left and lenient ones swipe right`() {
        assertEquals(SwipeDirection.LEFT, SwipeDirection.forRating(Rating.AGAIN))
        assertEquals(SwipeDirection.LEFT, SwipeDirection.forRating(Rating.HARD))
        assertEquals(SwipeDirection.RIGHT, SwipeDirection.forRating(Rating.GOOD))
        assertEquals(SwipeDirection.RIGHT, SwipeDirection.forRating(Rating.EASY))
    }

    /**
     * Presses the card and crosses the touch slop, which is where the card is picked up.
     * @return the x the card's movement is measured from
     */
    private fun beginDrag(towardsRight: Boolean): Float {
        dispatch(MotionEvent.ACTION_DOWN, START_X)
        val slop = ViewConfiguration.get(cardView.context).scaledTouchSlop + 1f
        val origin = if (towardsRight) START_X + slop else START_X - slop
        dispatch(MotionEvent.ACTION_MOVE, origin)
        return origin
    }

    private fun drag(deltaX: Float) {
        val end = beginDrag(towardsRight = deltaX > 0) + deltaX
        dispatch(MotionEvent.ACTION_MOVE, end)
        dispatch(MotionEvent.ACTION_UP, end)
    }

    private fun dispatch(
        action: Int,
        x: Float,
        y: Float = START_Y,
    ) {
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(now, now, action, x, y, 0)
        cardView.dispatchTouchEvent(event)
        event.recycle()
    }

    companion object {
        private const val CARD_WIDTH = 1000
        private const val CARD_HEIGHT = 1600
        private const val START_X = 500f
        private const val START_Y = 800f
    }
}
