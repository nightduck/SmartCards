// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.ichi2.anki.dialogs.decklanguage.DeckLanguageDialogViewModel.Companion.ARG_DECK_ID
import com.ichi2.anki.dialogs.savedStateHandleOf
import com.ichi2.anki.libanki.Consts.DEFAULT_DECK_ID
import com.ichi2.anki.libanki.language
import com.ichi2.anki.libanki.testutils.AnkiTest
import com.ichi2.testutils.JvmTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests [DeckLanguageDialogViewModel]: reading the deck's language and writing the user's choice
 * back to the collection.
 */
@RunWith(AndroidJUnit4::class)
class DeckLanguageDialogViewModelTest : JvmTest() {
    @Test
    fun `deck name is loaded`() =
        runViewModelTest {
            assertThat(flowOfDeckName.value, equalTo("Default"))
        }

    @Test
    fun `a deck with no language starts unselected`() =
        runViewModelTest {
            assertThat(flowOfSelectedTag.value, nullValue())
        }

    @Test
    fun `an existing language is loaded`() =
        runViewModelTest(initialLanguage = "fr") {
            assertThat(flowOfSelectedTag.value, equalTo("fr"))
        }

    @Test
    fun `selecting a language writes it to the collection`() =
        runViewModelTest {
            setLanguage("ja").join()

            assertThat(defaultDeck.language, equalTo("ja"))
            assertThat(flowOfSelectedTag.value, equalTo("ja"))
        }

    @Test
    fun `selecting 'no language' clears an existing language`() =
        runViewModelTest(initialLanguage = "fr") {
            setLanguage(null).join()

            assertThat(defaultDeck.language, nullValue())
            assertThat(flowOfSelectedTag.value, nullValue())
        }

    @Test
    fun `selecting 'no language' on a deck which has none is a no-op`() =
        runViewModelTest {
            setLanguage(null).join()

            assertThat(defaultDeck.language, nullValue())
        }

    @Test
    fun `a save is announced so the dialog can close`() =
        runViewModelTest {
            flowOfSaved.test {
                setLanguage("de").join()
                assertThat(awaitItem(), equalTo("de"))
            }
        }

    @Test
    fun `the deck name is unchanged by setting a language`() =
        runViewModelTest {
            setLanguage("de").join()

            assertThat(defaultDeck.name, equalTo("Default"))
        }

    private val AnkiTest.defaultDeck
        get() = col.decks.getLegacy(DEFAULT_DECK_ID)!!

    private fun runViewModelTest(
        initialLanguage: String? = null,
        testBody: suspend DeckLanguageDialogViewModel.() -> Unit,
    ) = runTest {
        if (initialLanguage != null) {
            col.decks.getLegacy(DEFAULT_DECK_ID)!!.let { deck ->
                deck.language = initialLanguage
                col.decks.save(deck)
            }
        }
        val viewModel = DeckLanguageDialogViewModel(savedStateHandleOf(ARG_DECK_ID to DEFAULT_DECK_ID))
        testBody(viewModel)
    }
}
