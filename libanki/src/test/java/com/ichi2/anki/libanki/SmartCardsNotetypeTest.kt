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

    private fun addSampleNote(): Note {
        val notetype = col.createSmartCardsNoteType()
        val note =
            col.newNote(notetype).apply {
                setItem("Word", "perro")
                setItem("Translation", "dog")
                setItem("Phonetic Spelling", "ˈpe.ro")
                setItem("Example Sentence", "El perro corre en el parque.")
            }
        col.addNote(note)
        return note
    }
}
