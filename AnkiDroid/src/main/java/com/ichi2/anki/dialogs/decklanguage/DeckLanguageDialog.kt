// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ichi2.anki.R
import com.ichi2.anki.analytics.AnalyticsDialogFragment
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.snackbar.showSnackbar
import com.ichi2.compose.theme.AnkiDroidTheme
import kotlinx.coroutines.launch

/** Gap between the dialog's surface and the screen edge. */
private val DialogMargin = 24.dp

/** Keeps the dialog from stretching the full width of a tablet. */
private val MaxDialogWidth = 560.dp

/**
 * Lets the user assign a language to a deck, or clear the one it has.
 *
 * The value is a BCP 47 tag stored on the deck itself (see
 * [com.ichi2.anki.libanki.language]), so it syncs with the deck and is deleted with it. This is
 * unrelated to `MetaDB`'s device-local text-to-speech language table.
 *
 * The content is Compose ([DeckLanguagePicker]), per the toolkit decision for the UI overhaul:
 * new surfaces are Compose, existing XML screens stay XML. The window itself is left transparent
 * so the Compose [androidx.compose.material3.Surface] provides the whole dialog, rather than
 * painting a Material 3 sheet on top of the platform dialog's own background.
 */
class DeckLanguageDialog : AnalyticsDialogFragment() {
    private val viewModel: DeckLanguageDialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        require(requireArguments().containsKey(DeckLanguageDialogViewModel.ARG_DECK_ID)) { "Missing argument deck id" }
        observeSaves()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AnkiDroidTheme {
                    val deckName by viewModel.flowOfDeckName.collectAsState()
                    val selectedTag by viewModel.flowOfSelectedTag.collectAsState()
                    val loaded by viewModel.flowOfLoaded.collectAsState()
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = DialogMargin),
                    ) {
                        DeckLanguagePicker(
                            deckName = deckName.orEmpty(),
                            selectedTag = selectedTag,
                            isLoading = !loaded,
                            onLanguageSelected = { tag -> viewModel.setLanguage(tag) },
                            onClose = { dismiss() },
                            modifier = Modifier.fillMaxWidth().widthIn(max = MaxDialogWidth),
                        )
                    }
                }
            }
        }

    override fun onStart() {
        super.onStart()
        // the Compose Surface draws the dialog's background, including its rounded corners
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    /** Confirms the write, then closes: nothing on the deck list reflects the language yet. */
    private fun observeSaves() {
        lifecycleScope.launch {
            viewModel.flowOfSaved.collect { tag ->
                // anchored on the activity, not this fragment: the dialog's own view is about
                // to be destroyed, and it isn't inside a CoordinatorLayout
                val activity = requireActivity()
                val message =
                    if (tag == null) {
                        getString(R.string.deck_language_cleared)
                    } else {
                        getString(R.string.deck_language_set, displayNameForTag(tag))
                    }
                dismiss()
                activity.showSnackbar(message)
            }
        }
    }

    companion object {
        /** Fragment tag; deliberately not the shared `"dialog"` tag. @see DeckPicker */
        const val TAG = "deckLanguageDialog"

        fun newInstance(deckId: DeckId): DeckLanguageDialog =
            DeckLanguageDialog().apply {
                arguments =
                    Bundle().apply {
                        putLong(DeckLanguageDialogViewModel.ARG_DECK_ID, deckId)
                    }
            }
    }
}
