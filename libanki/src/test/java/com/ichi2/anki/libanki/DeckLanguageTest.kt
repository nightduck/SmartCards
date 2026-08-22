// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.libanki

import com.ichi2.anki.libanki.testutils.InMemoryAnkiTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Tests for [Deck.language], SmartCards' per-deck [BCP 47](https://www.rfc-editor.org/info/bcp47)
 * language tag.
 *
 * `DecksTest` already pins the underlying mechanism (an unrecognised key survives a
 * [Decks.save]/[Decks.getLegacy] round-trip); these tests cover the accessor built on it.
 */
class DeckLanguageTest : InMemoryAnkiTest() {
    @Test
    fun `a new deck has no language`() {
        val deck = col.decks.getLegacy(addDeck("deck"))!!

        assertNull(deck.language)
        assertFalse(deck.has(DECK_LANGUAGE_KEY), "no key is written for a deck without a language")
    }

    @Test
    fun `language survives a save round-trip`() {
        val did = addDeck("deck")
        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = "fr"
            col.decks.save(deck)
        }

        assertEquals("fr", col.decks.getLegacy(did)!!.language)
    }

    @Test
    fun `a region-qualified tag is stored verbatim`() {
        val did = addDeck("deck")
        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = "pt-BR"
            col.decks.save(deck)
        }

        assertEquals("pt-BR", col.decks.getLegacy(did)!!.language)
    }

    @Test
    fun `setting a language does not disturb the rest of the deck`() {
        val did = addDeck("deck")
        val before = col.decks.getLegacy(did)!!
        before.description = "a description"
        before.language = "de"
        col.decks.save(before)

        val after = col.decks.getLegacy(did)!!
        assertEquals("deck", after.name)
        assertEquals("a description", after.description)
        assertEquals(before.conf, after.conf)
        assertEquals(before.collapsed, after.collapsed)
    }

    @Test
    fun `language can be replaced`() {
        val did = addDeck("deck")
        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = "fr"
            col.decks.save(deck)
        }
        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = "es"
            col.decks.save(deck)
        }

        assertEquals("es", col.decks.getLegacy(did)!!.language)
    }

    @Test
    fun `setting null clears the language`() {
        val did = addDeck("deck")
        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = "fr"
            col.decks.save(deck)
        }

        col.decks.getLegacy(did)!!.let { deck ->
            deck.language = null
            assertFalse(deck.has(DECK_LANGUAGE_KEY), "clearing removes the key rather than blanking it")
            col.decks.save(deck)
        }

        assertNull(col.decks.getLegacy(did)!!.language)
    }

    @Test
    fun `a blank tag is treated as no language`() {
        val deck = col.decks.getLegacy(addDeck("deck"))!!
        deck.language = "   "

        assertNull(deck.language)
        assertFalse(deck.has(DECK_LANGUAGE_KEY))
    }

    @Test
    fun `a language is per-deck`() {
        val french = addDeck("french")
        val german = addDeck("german")
        col.decks.getLegacy(french)!!.let { deck ->
            deck.language = "fr"
            col.decks.save(deck)
        }

        assertEquals("fr", col.decks.getLegacy(french)!!.language)
        assertNull(col.decks.getLegacy(german)!!.language, "sibling decks are unaffected")
    }
}
