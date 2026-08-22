// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.ui.windows.reviewer

import android.view.View
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.R
import com.ichi2.anki.RobolectricTest
import com.ichi2.anki.previewer.CardViewerActivity
import com.ichi2.anki.settings.Prefs
import com.ichi2.anki.settings.enums.ToolbarPosition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers #22: back-to-home, undo, edit and delete are fixed reviewer chrome and must stay
 * accessible directly from the review screen regardless of [Prefs.toolbarPosition].
 */
@RunWith(AndroidJUnit4::class)
class ReviewerFragmentChromeTest : RobolectricTest() {
    @Test
    fun `ToolbarPosition NONE keeps back, undo, edit and delete visible but hides counts, timer and menu`() {
        ensureCollectionLoadIsSynchronous()
        Prefs.toolbarPosition = ToolbarPosition.NONE

        val intent = ReviewerFragment.getIntent(targetContext)
        ActivityScenario.launch<CardViewerActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                fun visible(id: Int) = activity.findViewById<View>(id).isVisible

                assertTrue("back_button should stay visible", visible(R.id.back_button))
                assertTrue("undo_button should stay visible", visible(R.id.undo_button))
                assertTrue("edit_button should stay visible", visible(R.id.edit_button))
                assertTrue("delete_button should stay visible", visible(R.id.delete_button))

                assertFalse("study_counts should be hidden", visible(R.id.study_counts))
                assertFalse("timer should be hidden", visible(R.id.timer))
                assertFalse("reviewer_menu_view should be hidden", visible(R.id.reviewer_menu_view))
            }
        }
    }

    @Test
    fun `ToolbarPosition TOP shows the full tools_layout`() {
        ensureCollectionLoadIsSynchronous()
        Prefs.toolbarPosition = ToolbarPosition.TOP

        val intent = ReviewerFragment.getIntent(targetContext)
        ActivityScenario.launch<CardViewerActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                fun visible(id: Int) = activity.findViewById<View>(id).isVisible

                assertTrue(visible(R.id.back_button))
                assertTrue(visible(R.id.undo_button))
                assertTrue(visible(R.id.edit_button))
                assertTrue(visible(R.id.delete_button))
                assertTrue(visible(R.id.tools_layout))
            }
        }
    }
}
