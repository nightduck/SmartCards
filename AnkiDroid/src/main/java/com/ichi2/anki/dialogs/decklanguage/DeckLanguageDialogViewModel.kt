// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.libanki.language
import com.ichi2.anki.utils.ext.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Reads and writes the language of a single deck for [DeckLanguageDialog].
 *
 * The dialog writes on selection rather than behind a 'save' button, so there is no dirty state
 * to track: [setLanguage] persists and then asks the dialog to close.
 */
class DeckLanguageDialogViewModel(
    private val stateHandle: SavedStateHandle,
) : ViewModel() {
    val deckId: DeckId
        get() = requireNotNull(stateHandle.get<DeckId>(ARG_DECK_ID)) { "missing argument: $ARG_DECK_ID" }

    /** `null` until the deck has been loaded. */
    val flowOfDeckName = MutableStateFlow<String?>(null)

    /** The deck's current language tag, or `null` for "no language set". */
    val flowOfSelectedTag = MutableStateFlow<String?>(null)

    /** `false` until [flowOfDeckName] and [flowOfSelectedTag] reflect the collection. */
    val flowOfLoaded = MutableStateFlow(false)

    private val mutableFlowOfSaved = MutableSharedFlow<String?>()

    /** Emits the newly saved tag (`null` when cleared) once the write has completed. */
    val flowOfSaved = mutableFlowOfSaved.asSharedFlow()

    init {
        viewModelScope.launch {
            val deck = withCol { decks.getLegacy(deckId) }
            if (deck == null) {
                Timber.w("deck %d no longer exists", deckId)
                return@launch
            }
            flowOfDeckName.value = deck.name
            flowOfSelectedTag.value = deck.language
            flowOfLoaded.value = true
        }
    }

    /**
     * Sets the deck's language to [tag], or clears it if [tag] is `null`, then emits on
     * [flowOfSaved].
     */
    fun setLanguage(tag: String?) =
        viewModelScope.launch {
            Timber.i("setting language of deck %d to '%s'", deckId, tag)
            withCol { decks.update(deckId) { language = tag } }
            flowOfSelectedTag.value = tag
            mutableFlowOfSaved.emit(tag)
        }

    companion object {
        const val ARG_DECK_ID = "deckId"
    }
}
