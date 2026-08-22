// SPDX-License-Identifier: GPL-3.0-or-later
package com.ichi2.anki.libanki

import com.ichi2.anki.libanki.testutils.InMemoryAnkiTest
import com.ichi2.anki.libanki.testutils.ext.addNote
import com.ichi2.anki.libanki.testutils.ext.createSmartCardsNoteType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test

class SmartCardsNotetypeTest : InMemoryAnkiTest() {
    @Test
    fun `has the expected fields in order`() {
        val notetype = col.createSmartCardsNoteType()
        assertThat(
            notetype.fieldsNames,
            equalTo(listOf("Word", "Translation", "Phonetic Spelling", "Audio", "Image", "Example Sentence", "Explainer")),
        )
    }

    @Test
    fun `has the expected templates`() {
        val notetype = col.createSmartCardsNoteType()
        assertThat(
            notetype.templatesNames,
            equalTo(listOf("Word → Translation", "Translation → Word", "Listening")),
        )
    }

    @Test
    fun `a note produces 3 cards`() {
        val note = addSampleNote()
        assertThat(note.numberOfCards(col), equalTo(3))
    }

    @Test
    fun `word prompt card shows word on front and translation on back`() {
        val note = addSampleNote()
        val card = note.cards(col)[0]
        assertThat(card.question(col), containsString("perro"))
        assertThat(card.answer(col), containsString("dog"))
    }

    @Test
    fun `translation prompt card shows translation on front and word on back`() {
        val note = addSampleNote()
        val card = note.cards(col)[1]
        assertThat(card.question(col), containsString("dog"))
        assertThat(card.question(col), not(containsString("perro")))
        assertThat(card.answer(col), containsString("perro"))
    }

    @Test
    fun `listening card spoilers the word on the front`() {
        val note = addSampleNote()
        val card = note.cards(col)[2]
        val question = card.question(col)
        assertThat(question, containsString("<details"))
        assertThat(question, containsString("perro"))
        assertThat(question, not(containsString("dog")))
        assertThat(card.answer(col), containsString("dog"))
    }

    @Test
    fun `explainer is emitted as a details disclosure the app can style`() {
        val note = addSampleNote()
        // `#qa details.explainer` in ankidroid.css draws this as the dictionary panel, and
        // `isInteractable()` in ankidroid-reviewer.js exempts its SUMMARY from gesture capture.
        // Both key off this markup, and neither can be reached from a unit test.
        for (card in note.cards(col)) {
            assertThat(card.answer(col), containsString("""<details class="explainer">"""))
            assertThat(card.answer(col), containsString("<summary>More</summary>"))
        }
    }

    @Test
    fun `explainer is left unstyled by the note type`() {
        // The panel's colours come from the app's theme via ankidroid.css. Styling the explainer
        // here would freeze a colour into every collection the note type is added to - and win,
        // since a note type's CSS is injected after ankidroid.css.
        val notetype = col.createSmartCardsNoteType()
        assertThat(notetype.css, not(containsString("details.explainer")))
    }

    private fun addSampleNote(): Note {
        val notetype = col.createSmartCardsNoteType()
        val note =
            col.newNote(notetype).apply {
                setItem("Word", "perro")
                setItem("Translation", "dog")
                setItem("Phonetic Spelling", "ˈpe.ro")
                setItem("Example Sentence", "El perro corre en el parque.")
                setItem("Explainer", "Masculine noun. Feminine: perra.")
            }
        col.addNote(note)
        return note
    }
}
