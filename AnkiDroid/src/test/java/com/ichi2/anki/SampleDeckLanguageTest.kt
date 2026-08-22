// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki

import androidx.test.ext.junit.runners.AndroidJUnit4
import anki.import_export.ImportAnkiPackageOptions
import com.ichi2.anki.libanki.language
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Guards the Spanish language tag on the checked-in sample deck in `tools/test-decks/`.
 *
 * That deck is a manual test asset, but this is also the only end-to-end evidence that a
 * [com.ichi2.anki.libanki.language] set outside the app survives the backend's *import* path.
 * `DeckLanguageTest` covers a save/load round trip inside a single collection, which is the
 * weaker claim. The tag lives inside a binary archive nobody will reopen by hand, so without
 * this a regenerated deck could silently drop it.
 *
 * ### Why `withScheduling`
 *
 * The tag is carried in the deck record's `other` blob, and the importer only reuses the
 * packaged deck record when scheduling is included. With the default options it builds a fresh
 * deck from the name alone, and any application-defined key is dropped — so importing this deck
 * without scheduling gives a deck with **no** language. That is backend behaviour, not something
 * this code chooses; it is asserted here in its working form so the asset itself is what's under
 * test.
 *
 * Runs on-disk: the package carries media, and importing it needs a real collection folder.
 */
@RunWith(AndroidJUnit4::class)
class SampleDeckLanguageTest : RobolectricTest() {
    override fun getCollectionStorageMode() = CollectionStorageMode.ON_DISK

    @Test
    fun `the sample deck imports with its language set to Spanish`() {
        // a test's working directory is its module directory, so this resolves to `<repo>/tools`
        val apkg = File("../tools/test-decks/smartcards_vocabulary_sample.apkg")
        assertTrue(apkg.exists(), "missing test asset: ${apkg.absolutePath}")

        col.importAnkiPackage(
            apkg.absolutePath,
            ImportAnkiPackageOptions.newBuilder().setWithScheduling(true).build(),
        )

        val deck = col.decks.byName(SAMPLE_DECK_NAME)
        assertNotNull(deck, "the sample deck should have been imported")
        assertEquals("es", deck.language, "the sample deck should be tagged as Spanish")
    }

    companion object {
        private const val SAMPLE_DECK_NAME = "SmartCards Vocabulary Sample"
    }
}
