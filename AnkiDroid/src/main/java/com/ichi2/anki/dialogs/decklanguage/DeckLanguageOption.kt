// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import java.text.Collator
import java.util.Locale

/**
 * A language which may be assigned to a deck.
 *
 * @param tag a [BCP 47](https://www.rfc-editor.org/info/bcp47) language tag, as stored in
 * [com.ichi2.anki.libanki.language]
 * @param displayName the language's name, in the reader's own language ("German", "Deutsch", ...)
 */
@Immutable
data class DeckLanguageOption(
    val tag: String,
    val displayName: String,
)

/**
 * Every language the platform knows about, as language-only tags (`fr`, `pt`, ...), sorted by
 * [DeckLanguageOption.displayName] using [displayLocale]'s collation rules.
 *
 * Region and script subtags are deliberately not offered: a deck teaches "Portuguese", and the
 * extra several hundred region-qualified entries would bury the languages a user is actually
 * looking for. A region-qualified tag set by other means (`pt-BR`) is still stored and read back
 * verbatim — see [com.ichi2.anki.libanki.language].
 *
 * Tags are produced with [Locale.toLanguageTag], so obsolete ISO 639 codes are normalised
 * (`iw` -> `he`, `in` -> `id`, `ji` -> `yi`).
 *
 * @param availableLocales injectable for tests; defaults to the platform's locale list
 */
fun deckLanguageOptions(
    displayLocale: Locale = Locale.getDefault(),
    availableLocales: Array<Locale> = Locale.getAvailableLocales(),
): List<DeckLanguageOption> {
    val byTag = LinkedHashMap<String, DeckLanguageOption>()
    for (locale in availableLocales) {
        if (locale.language.isBlank()) continue
        val languageOnly = Locale.forLanguageTag(locale.language)
        val tag = languageOnly.toLanguageTag()
        if (tag.isBlank() || tag == UNDETERMINED_LANGUAGE_TAG) continue
        byTag.getOrPut(tag) {
            DeckLanguageOption(tag = tag, displayName = languageOnly.getDisplayLanguage(displayLocale))
        }
    }
    val collator = Collator.getInstance(displayLocale)
    return byTag.values.sortedWith(compareBy(collator) { it.displayName })
}

/**
 * The subset of the receiver matching [query] on either name or tag, case-insensitively.
 * A blank [query] matches everything.
 */
// LocaleRootUsage: [DeckLanguageOption.tag] is a machine identifier, not user-facing text — it is
// always ASCII, and folding it in the reader's locale would break a Turkish user typing "i".
@SuppressLint("LocaleRootUsage")
fun List<DeckLanguageOption>.matching(
    query: String,
    displayLocale: Locale = Locale.getDefault(),
): List<DeckLanguageOption> {
    val needle = query.trim().lowercase(displayLocale)
    if (needle.isEmpty()) return this
    return filter {
        it.displayName.lowercase(displayLocale).contains(needle) ||
            it.tag.lowercase(Locale.ROOT).startsWith(needle)
    }
}

/**
 * The receiver, guaranteed to contain an entry for [tag].
 *
 * [deckLanguageOptions] only offers language-only tags, but a deck's stored tag may be anything —
 * set on another device, by a future SmartCards version, or by an Anki Desktop add-on. Prepending
 * the missing entry means the current value is always visible and always shown as selected,
 * instead of the dialog silently looking as though no language were set.
 *
 * A `null` or already-present [tag] returns the receiver unchanged.
 */
fun List<DeckLanguageOption>.withSelected(
    tag: String?,
    displayLocale: Locale = Locale.getDefault(),
): List<DeckLanguageOption> {
    if (tag == null || any { it.tag == tag }) return this
    return listOf(DeckLanguageOption(tag = tag, displayName = displayNameForTag(tag, displayLocale))) + this
}

/**
 * The name to show for [tag], or the tag itself if the platform has no name for it — which is how
 * a tag set on another device, or by a future SmartCards version, still displays sensibly.
 */
fun displayNameForTag(
    tag: String,
    displayLocale: Locale = Locale.getDefault(),
): String = Locale.forLanguageTag(tag).getDisplayName(displayLocale).ifBlank { tag }

/** BCP 47's "undetermined" language; never a meaningful choice for a deck. */
private const val UNDETERMINED_LANGUAGE_TAG = "und"
