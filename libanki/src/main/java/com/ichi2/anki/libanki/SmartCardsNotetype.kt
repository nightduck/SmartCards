// SPDX-License-Identifier: GPL-3.0-or-later
package com.ichi2.anki.libanki

/**
 * SmartCards' custom "audio prompt" vocabulary note type (see [issue #10](https://github.com/nightduck/SmartCards/issues/10)).
 *
 * Unlike "stock" note types (Basic, Cloze, ...), which are baked into the native backend and
 * exposed via [anki.notetypes.StockNotetype.Kind], this note type is defined entirely in Kotlin
 * and built with the same field/template APIs a user's custom note type would use - see
 * [com.ichi2.anki.notetype.AddNewNotesType] for where it's offered to users.
 */
const val SMARTCARDS_NOTETYPE_NAME = "SmartCards Vocabulary"

const val SMARTCARDS_FIELD_WORD = "Word"
const val SMARTCARDS_FIELD_TRANSLATION = "Translation"
const val SMARTCARDS_FIELD_PHONETIC_SPELLING = "Phonetic Spelling"
const val SMARTCARDS_FIELD_AUDIO = "Audio"
const val SMARTCARDS_FIELD_IMAGE = "Image"
const val SMARTCARDS_FIELD_EXAMPLE_SENTENCE = "Example Sentence"
const val SMARTCARDS_FIELD_EXPLAINER = "Explainer"

/** Field order; index 0 is the sort field, mirroring the "Front" convention of Basic. */
private val SMARTCARDS_FIELD_NAMES =
    listOf(
        SMARTCARDS_FIELD_WORD,
        SMARTCARDS_FIELD_TRANSLATION,
        SMARTCARDS_FIELD_PHONETIC_SPELLING,
        SMARTCARDS_FIELD_AUDIO,
        SMARTCARDS_FIELD_IMAGE,
        SMARTCARDS_FIELD_EXAMPLE_SENTENCE,
        SMARTCARDS_FIELD_EXPLAINER,
    )

private const val ANSWER_EXTRAS =
    """{{#Example Sentence}}<div class="example">{{Example Sentence}}</div>{{/Example Sentence}}
{{#Explainer}}<details class="explainer"><summary>More</summary>{{Explainer}}</details>{{/Explainer}}"""

private val SMARTCARDS_TEMPLATES =
    listOf(
        Triple(
            "Word → Translation",
            // qfmt
            """<div class="word">{{Word}}</div>
{{#Phonetic Spelling}}<div class="phonetic">/{{Phonetic Spelling}}/</div>{{/Phonetic Spelling}}
{{Audio}}
{{#Image}}<div class="image">{{Image}}</div>{{/Image}}""",
            // afmt
            """{{FrontSide}}
<hr id="answer">
<div class="translation">{{Translation}}</div>
$ANSWER_EXTRAS""",
        ),
        Triple(
            "Translation → Word",
            """<div class="translation">{{Translation}}</div>
{{#Image}}<div class="image">{{Image}}</div>{{/Image}}""",
            """{{FrontSide}}
<hr id="answer">
<div class="word">{{Word}}</div>
{{#Phonetic Spelling}}<div class="phonetic">/{{Phonetic Spelling}}/</div>{{/Phonetic Spelling}}
{{Audio}}
$ANSWER_EXTRAS""",
        ),
        Triple(
            "Listening",
            """{{Audio}}
{{#Image}}<div class="image">{{Image}}</div>{{/Image}}
<details class="word-spoiler"><summary>Reveal word</summary><div class="word">{{Word}}</div></details>""",
            """{{FrontSide}}
<hr id="answer">
<div class="translation">{{Translation}}</div>
$ANSWER_EXTRAS""",
        ),
    )

private const val SMARTCARDS_CSS =
    """.card {
    font-family: arial;
    font-size: 20px;
    text-align: center;
    color: black;
    background-color: white;
}
.word { font-size: 28px; font-weight: bold; margin: 8px 0; }
.translation { font-size: 24px; margin: 8px 0; }
.phonetic { color: #666; font-style: italic; margin: 4px 0; }
.example { font-size: 16px; color: #444; margin-top: 12px; }
.image img { max-width: 100%; max-height: 240px; }
details.word-spoiler summary {
    cursor: pointer;
    color: #0066cc;
}
/* The explainer disclosure is deliberately left unstyled here: the app draws it as a dictionary
   panel from `ankidroid.css`, using theme colours a collection can't know about. Styling it here
   would freeze one set of colours into every collection the note type is added to. */"""

/**
 * Builds (but does not persist) SmartCards' custom "audio prompt" vocabulary note type.
 *
 * The caller is responsible for persisting the returned note type, e.g. via
 * `addNotetypeLegacy(BackendUtils.toJsonBytes(notetype))`.
 */
fun Collection.newSmartCardsNotetype(name: String = SMARTCARDS_NOTETYPE_NAME): NotetypeJson {
    val notetype = notetypes.new(name)
    for (fieldName in SMARTCARDS_FIELD_NAMES) {
        notetypes.addField(notetype, notetypes.newField(fieldName))
    }
    for ((templateName, qfmt, afmt) in SMARTCARDS_TEMPLATES) {
        val template = notetypes.newTemplate(templateName)
        template.qfmt = qfmt
        template.afmt = afmt
        notetypes.add_template(notetype, template)
    }
    notetype.css = SMARTCARDS_CSS
    return notetype
}
