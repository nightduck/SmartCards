// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.ui.windows.reviewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.preferences.reviewer.ViewerAction
import com.ichi2.testutils.JvmTest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the "delete" chrome control promoted to a fixed, always-visible reviewer button by #22:
 * deleting a note from the review screen must ask for confirmation first, and only actually
 * delete once that confirmation is given.
 */
@RunWith(AndroidJUnit4::class)
class ReviewerViewModelDeleteTest : JvmTest() {
    private lateinit var viewModel: ReviewerViewModel

    @Before
    override fun setUp() {
        super.setUp()
        addBasicNote("Front text", "Back text")
        viewModel = ReviewerViewModel(SavedStateHandle())
    }

    @After
    override fun tearDown() {
        viewModel.server.stop()
        viewModel.viewModelScope.cancel()
        super.tearDown()
    }

    @Test
    fun `delete action requests confirmation instead of deleting immediately`() =
        runTest {
            val noteCountBefore = col.noteCount()

            viewModel.executeAction(ViewerAction.DELETE)
            advanceUntilIdle()

            assertEquals(
                "note should still exist until the user confirms deletion",
                noteCountBefore,
                col.noteCount(),
            )
        }

    @Test
    fun `delete action emits the note's question for the confirmation dialog`() =
        runTest {
            // deleteNoteConfirmationFlow has no replay, so the collector must be subscribed
            // before executeAction runs, or the emission is missed (see MutableSharedFlow docs).
            var receivedQuestion: String? = null
            val collector =
                launch {
                    receivedQuestion = viewModel.deleteNoteConfirmationFlow.first()
                }
            advanceUntilIdle()

            viewModel.executeAction(ViewerAction.DELETE)
            advanceUntilIdle()
            collector.join()

            assertEquals("Front text", receivedQuestion)
        }

    @Test
    fun `confirming a pending delete removes the note`() =
        runTest {
            val noteCountBefore = col.noteCount()

            viewModel.executeAction(ViewerAction.DELETE)
            advanceUntilIdle()
            viewModel.deleteNoteConfirmed()
            advanceUntilIdle()

            assertEquals(noteCountBefore - 1, col.noteCount())
        }
}
