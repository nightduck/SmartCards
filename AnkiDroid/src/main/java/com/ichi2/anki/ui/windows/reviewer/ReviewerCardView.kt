// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 Oren Bell <virtualoren@gmail.com>

package com.ichi2.anki.ui.windows.reviewer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import anki.scheduler.CardAnswer.Rating
import com.google.android.material.card.MaterialCardView
import com.ichi2.anki.cardviewer.Gesture
import com.ichi2.anki.common.android.Animations
import com.ichi2.anki.common.annotations.NeedsTest
import kotlin.math.abs

/**
 * The physical-looking flashcard of the study screen.
 *
 * It owns the three card motions described in #14:
 * * [onAnswerShown] flips the card over when the answer is revealed.
 * * [swipeOut] throws the card off screen when a rating button is pressed.
 * * A horizontal drag follows the finger, and on release either throws the card off screen or
 *   springs it back.
 *
 * A thrown card stays off screen until the fragment reports the replacement content via
 * [onAnswerShown]/[onNextCardShown], so the swap is never visible mid-flight.
 *
 * All motion is skipped when [Animations.areAnimationsEnabled] is false. Dragging still tracks
 * the finger in that case, since it is direct manipulation rather than an animation.
 */
class ReviewerCardView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
    ) : MaterialCardView(context, attrs, defStyleAttr) {
        /**
         * Invoked when a drag is released past the swipe threshold.
         * Should return whether the gesture was consumed; if it wasn't, the card springs back.
         */
        var onSwipe: ((Gesture) -> Boolean)? = null

        /**
         * Whether the card's own content can absorb a horizontal scroll of the given direction
         * (negative for a scroll towards the start), in which case the drag is left to it.
         */
        var canContentScrollHorizontally: (Int) -> Boolean = { false }

        /** The view holding the card content, faded out while the card is edge-on during a flip. */
        var contentView: View? = null

        /** Set to false while another child owns horizontal touches, e.g. the whiteboard. */
        var isDragEnabled = true

        /**
         * Whether the answer side is showing.
         *
         * Only then does a drag pick the card up: on the question side there is nothing to rate,
         * so a swipe is left to the JavaScript gesture handler and just reveals the answer, the
         * same as a tap does.
         */
        var isAnswerShown = false

        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private var dragStartX = 0f
        private var dragStartY = 0f
        private var isDragging = false

        /** Set while the card is off screen or on its way there. */
        private var isThrown = false

        /** Set while the throw-out animation is still playing. */
        private var isThrowInFlight = false

        /** Set once the replacement content has reached the card. */
        private var hasReplacementContent = false

        private val settleInFallback = Runnable { settleIn() }

        private val areAnimationsEnabled: Boolean
            get() = !isInEditMode && Animations.areAnimationsEnabled(context)

        init {
            // Without this the perspective of a rotationY is extreme enough to warp the card.
            cameraDistance = CAMERA_DISTANCE_DP * resources.displayMetrics.density
        }

        /** Throws the card off screen towards [direction]. Pairs with [onNextCardShown]. */
        @NeedsTest("the throw and settle-in motion itself; needs a device, see #21")
        fun swipeOut(direction: SwipeDirection) {
            if (isThrown || !areAnimationsEnabled) return
            throwOut(direction)
        }

        /** Reports that the answer side has been loaded into the card. */
        @NeedsTest("the flip motion itself; needs a device, see #21")
        fun onAnswerShown() {
            if (completePendingSwipe()) return
            flip()
        }

        /** Reports that a new card's question has been loaded into the card. */
        fun onNextCardShown() {
            completePendingSwipe()
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            if (!isDragEnabled || !isAnswerShown || isThrown) return false
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartX = ev.rawX
                    dragStartY = ev.rawY
                    isDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (ev.pointerCount > 1) return false
                    val deltaX = ev.rawX - dragStartX
                    val deltaY = ev.rawY - dragStartY
                    val isHorizontal = abs(deltaX) > touchSlop && abs(deltaX) > abs(deltaY) * HORIZONTAL_BIAS
                    if (isHorizontal && !canContentScrollHorizontally(if (deltaX > 0) -1 else 1)) {
                        // Pick the card up wherever it currently is, so neither the touch slop
                        // nor an interrupted spring-back makes it jump.
                        animate().cancel()
                        dragStartX = ev.rawX - translationX
                        isDragging = true
                        parent?.requestDisallowInterceptTouchEvent(true)
                        return true
                    }
                }
            }
            return false
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            if (!isDragging) return super.onTouchEvent(ev)
            when (ev.actionMasked) {
                MotionEvent.ACTION_MOVE -> setDragOffset(ev.rawX - dragStartX)
                MotionEvent.ACTION_UP -> {
                    isDragging = false
                    onDragReleased()
                }
                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    springBack()
                }
            }
            return true
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            removeCallbacks(settleInFallback)
            animate().cancel()
            contentView?.animate()?.cancel()
        }

        private fun setDragOffset(offset: Float) {
            translationX = offset
            rotation = offset / width.coerceAtLeast(1) * MAX_DRAG_ROTATION
        }

        private fun onDragReleased() {
            val direction = if (translationX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
            val isPastThreshold = abs(translationX) > width * SWIPE_THRESHOLD_FRACTION
            if (!isPastThreshold || onSwipe?.invoke(direction.gesture) != true) {
                springBack()
                return
            }
            if (!areAnimationsEnabled) {
                setDragOffset(0f)
                return
            }
            throwOut(direction)
        }

        private fun springBack() {
            animate()
                .translationX(0f)
                .rotation(0f)
                .setDuration(if (areAnimationsEnabled) SPRING_BACK_DURATION else 0)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        private fun throwOut(direction: SwipeDirection) {
            isThrown = true
            isThrowInFlight = true
            hasReplacementContent = false
            // A rating can be given while a flip is still playing, so take the card back over
            animate().cancel()
            contentView?.animate()?.cancel()
            contentView?.alpha = 1f
            rotationY = 0f
            // The card keeps its opacity on the way out: fading it made the throw read as the
            // card blinking out rather than being flung off the screen edge.
            animate()
                .translationX(direction.sign * width * THROW_OUT_WIDTHS)
                .rotation(direction.sign * MAX_DRAG_ROTATION)
                .setDuration(THROW_OUT_DURATION)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    isThrowInFlight = false
                    settleInIfReady()
                }.start()
            // The content usually arrives well within this, but a failed action must not
            // leave the card stranded off screen.
            postDelayed(settleInFallback, CONTENT_TIMEOUT)
        }

        private fun completePendingSwipe(): Boolean {
            if (!isThrown) return false
            hasReplacementContent = true
            settleInIfReady()
            return true
        }

        /**
         * The replacement content usually arrives long before the card has finished being thrown.
         * Settling in early would cut the throw short and leave nothing to see, so the card waits
         * for both.
         */
        private fun settleInIfReady() {
            if (isThrowInFlight || !hasReplacementContent) return
            removeCallbacks(settleInFallback)
            settleIn()
        }

        /** Puts the card back at rest, whatever an interrupted animation left it in. */
        private fun resetTransforms() {
            translationX = 0f
            rotation = 0f
            rotationY = 0f
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }

        private fun settleIn() {
            isThrown = false
            isThrowInFlight = false
            hasReplacementContent = false
            animate().cancel()
            contentView?.animate()?.cancel()
            contentView?.alpha = 1f
            resetTransforms()
            if (!areAnimationsEnabled) return
            alpha = 0f
            scaleX = SETTLE_IN_SCALE
            scaleY = SETTLE_IN_SCALE
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(SETTLE_IN_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        /**
         * Turns the card over its vertical axis. The content is faded out for the first half so
         * that the answer replacing the question is never visible, whenever it happens to arrive.
         */
        private fun flip() {
            if (!areAnimationsEnabled) return
            animate().cancel()
            contentView?.animate()?.cancel()
            resetTransforms()

            fadeContentTo(0f, FLIP_HALF_DURATION)
            animate()
                .rotationY(FLIP_ANGLE)
                .setDuration(FLIP_HALF_DURATION)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    rotationY = -FLIP_ANGLE
                    animate()
                        .rotationY(0f)
                        .setDuration(FLIP_HALF_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    fadeContentTo(1f, FLIP_HALF_DURATION / 2, startDelay = FLIP_HALF_DURATION / 2)
                }.start()
        }

        private fun fadeContentTo(
            alpha: Float,
            duration: Long,
            startDelay: Long = 0,
        ) {
            contentView
                ?.animate()
                ?.alpha(alpha)
                ?.setStartDelay(startDelay)
                ?.setDuration(duration)
                ?.start()
        }

        enum class SwipeDirection(
            val sign: Float,
            val gesture: Gesture,
        ) {
            LEFT(-1f, Gesture.SWIPE_LEFT),
            RIGHT(1f, Gesture.SWIPE_RIGHT),
            ;

            companion object {
                /**
                 * #14 pairs the harsher ratings with a left swipe and the lenient ones with a
                 * right swipe, so a rating looks the same however it was given.
                 */
                fun forRating(rating: Rating): SwipeDirection =
                    when (rating) {
                        Rating.AGAIN, Rating.HARD -> LEFT
                        Rating.GOOD, Rating.EASY, Rating.UNRECOGNIZED -> RIGHT
                    }
            }
        }

        companion object {
            /** A drag must cover this fraction of the card's width to throw it off screen. */
            private const val SWIPE_THRESHOLD_FRACTION = 0.25f

            /**
             * How much more horizontal than vertical a drag must be to be taken as a swipe,
             * rather than left to the content to scroll.
             */
            private const val HORIZONTAL_BIAS = 1.5f

            /** Degrees the card tilts by when dragged a full card width. */
            private const val MAX_DRAG_ROTATION = 12f

            /** Card widths a thrown card travels, enough to clear the screen. */
            private const val THROW_OUT_WIDTHS = 1.5f

            private const val SETTLE_IN_SCALE = 0.94f
            private const val FLIP_ANGLE = 90f
            private const val CAMERA_DISTANCE_DP = 8000f

            private const val SPRING_BACK_DURATION = 220L

            // Slow enough that a rating given by button, where there is no drag leading into it,
            // still reads as the card being thrown off the screen
            private const val THROW_OUT_DURATION = 330L
            private const val SETTLE_IN_DURATION = 260L
            private const val FLIP_HALF_DURATION = 170L
            private const val CONTENT_TIMEOUT = 1500L
        }
    }
