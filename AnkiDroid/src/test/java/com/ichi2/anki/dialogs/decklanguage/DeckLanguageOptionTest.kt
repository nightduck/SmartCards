// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import android.annotation.SuppressLint
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.Test
import java.util.Locale

/** Tests the language list backing [DeckLanguagePicker]. */
class DeckLanguageOptionTest {
    private val english = Locale.ENGLISH

    private fun options(vararg locales: Locale) = deckLanguageOptions(displayLocale = english, availableLocales = arrayOf(*locales))

    @Test
    fun `options are language-only tags`() {
        val options = options(Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("pt-PT"))

        assertThat(options.map { it.tag }, contains("pt"))
    }

    @Test
    fun `a language appears once, however many regions offer it`() {
        val options =
            options(
                Locale.forLanguageTag("fr"),
                Locale.forLanguageTag("fr-CA"),
                Locale.forLanguageTag("fr-BE"),
            )

        assertThat(options, hasSize(1))
    }

    @Test
    fun `options are named in the display locale`() {
        val tag = Locale.forLanguageTag("de")

        assertThat(deckLanguageOptions(english, arrayOf(tag)).single().displayName, equalTo("German"))
        assertThat(deckLanguageOptions(Locale.GERMAN, arrayOf(tag)).single().displayName, equalTo("Deutsch"))
    }

    @Test
    fun `options are sorted by display name, not by tag`() {
        val options =
            options(
                Locale.forLanguageTag("de"),
                Locale.forLanguageTag("es"),
                Locale.forLanguageTag("ja"),
            )

        // by tag this would be de, es, ja
        assertThat(options.map { it.displayName }, contains("German", "Japanese", "Spanish"))
    }

    @Test
    fun `obsolete ISO 639 codes are normalised to their modern tag`() {
        // Locale.getLanguage() still reports the pre-1989 codes for these three
        val options = options(Locale.forLanguageTag("he"), Locale.forLanguageTag("id"), Locale.forLanguageTag("yi"))

        assertThat(options.map { it.tag }.sorted(), contains("he", "id", "yi"))
    }

    @Test
    // LocaleRootUsage: Locale.ROOT is the subject of this test, not a text-folding locale
    @SuppressLint("LocaleRootUsage")
    fun `locales without a language are skipped`() {
        assertThat(options(Locale.ROOT, Locale.forLanguageTag("und")), empty())
    }

    @Test
    fun `the real platform list is non-trivial and contains common languages`() {
        val tags = deckLanguageOptions(displayLocale = english).map { it.tag }

        assertThat(tags, hasItem("fr"))
        assertThat(tags, hasItem("ja"))
        assertThat(tags, not(hasItem("und")))
    }

    @Test
    fun `a blank query matches everything`() {
        val options = options(Locale.forLanguageTag("fr"), Locale.forLanguageTag("de"))

        assertThat(options.matching("  ", english), equalTo(options))
    }

    @Test
    fun `a query matches the display name, case-insensitively`() {
        val options = options(Locale.forLanguageTag("fr"), Locale.forLanguageTag("de"))

        assertThat(options.matching("gErM", english).map { it.tag }, contains("de"))
    }

    @Test
    fun `a query matches the start of a tag`() {
        val options = options(Locale.forLanguageTag("fr"), Locale.forLanguageTag("de"))

        assertThat(options.matching("fr", english).map { it.tag }, contains("fr"))
    }

    @Test
    fun `a query matching nothing returns nothing`() {
        val options = options(Locale.forLanguageTag("fr"))

        assertThat(options.matching("klingon", english), empty())
    }

    @Test
    fun `withSelected leaves the list alone when nothing is selected`() {
        val options = options(Locale.forLanguageTag("fr"))

        assertThat(options.withSelected(null, english), equalTo(options))
    }

    @Test
    fun `withSelected leaves the list alone when the selection is already offered`() {
        val options = options(Locale.forLanguageTag("fr"))

        assertThat(options.withSelected("fr", english), equalTo(options))
    }

    @Test
    fun `withSelected surfaces a tag the picker does not offer`() {
        val options = options(Locale.forLanguageTag("fr"))

        val withRegion = options.withSelected("pt-BR", english)

        assertThat(withRegion.map { it.tag }, contains("pt-BR", "fr"))
        assertThat(withRegion.first().displayName, equalTo("Portuguese (Brazil)"))
    }

    @Test
    fun `a tag the platform cannot parse displays as itself`() {
        assertThat(displayNameForTag("!! not a tag", english), equalTo("!! not a tag"))
    }
}
