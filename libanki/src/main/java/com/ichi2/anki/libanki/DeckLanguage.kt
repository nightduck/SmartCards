// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.libanki

/**
 * The legacy-deck-JSON key under which SmartCards stores a deck's language.
 *
 * Namespaced with a `smartcards` prefix so it can never collide with a key the backend (or an
 * Anki Desktop add-on) gives meaning to. See the "Arbitrary metadata survives a round-trip"
 * section of [Deck] for why an unrecognised key survives at all.
 */
const val DECK_LANGUAGE_KEY = "smartcardsLanguage"

/**
 * The [BCP 47](https://www.rfc-editor.org/info/bcp47) language tag of the language this deck
 * teaches (`"fr"`, `"pt-BR"`, ...), or `null` if no language has been set.
 *
 * "No language set" is represented by the key being absent, and setting `null` (or a blank tag)
 * removes it, so a deck that never had a language and a deck whose language was cleared are
 * indistinguishable — deliberately, since neither should render a flag.
 *
 * Reads and writes go through the legacy deck JSON, so a value set here round-trips through the
 * backend and travels with sync. Persist a change with [Decks.save] (or
 * `com.ichi2.anki.utils.ext.update`); mutating the [Deck] alone only changes the in-memory copy.
 *
 * Not to be confused with `MetaDB`'s `languages` table, which is a device-local text-to-speech
 * setting keyed per card-template ordinal and card side.
 */
var Deck.language: String?
    get() = optString(DECK_LANGUAGE_KEY, "").ifBlank { null }
    set(value) {
        val tag = value?.trim()
        if (tag.isNullOrEmpty()) {
            remove(DECK_LANGUAGE_KEY)
        } else {
            put(DECK_LANGUAGE_KEY, tag)
        }
    }
