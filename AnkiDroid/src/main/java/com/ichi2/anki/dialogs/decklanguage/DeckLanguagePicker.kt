// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.dialogs.decklanguage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.common.annotations.NeedsTest
import com.ichi2.compose.theme.AnkiDroidTheme
import com.ichi2.compose.theme.dimensions
import com.ichi2.compose.ui.preview.ThemePreviews

/** Cap on the list's height, so a long list can't push the buttons off a short screen. */
private val MaxListHeight = 320.dp

/** Height reserved for the spinner, so the dialog doesn't resize once the deck loads. */
private val LoadingHeight = 120.dp

/**
 * Lets the user pick the language a deck teaches, or state that it has none.
 *
 * Selection takes effect immediately — there is no confirm button — so [onLanguageSelected] is
 * expected to persist the value and close the dialog.
 *
 * Lives beside its dialog rather than in the shared compose package, which is an extraction
 * target for reusable pieces and so may not depend on feature code
 * (see `docs/development/compose.md`).
 *
 * @param deckName shown as a subtitle, so the user can tell which deck they are editing
 * @param selectedTag the deck's current language tag, or `null` for "no language set"
 * @param isLoading whether [deckName]/[selectedTag] are still being read from the collection
 * @param onLanguageSelected called with the chosen tag, or `null` for "no language"
 * @param onClose called when the user dismisses without choosing
 */
@NeedsTest("no Compose UI test: the project has no compose-ui-test/roborazzi-compose dependency yet")
@Composable
fun DeckLanguagePicker(
    deckName: String,
    selectedTag: String?,
    isLoading: Boolean,
    onLanguageSelected: (String?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    allOptions: List<DeckLanguageOption> = remember { deckLanguageOptions() },
) {
    var query by rememberSaveable { mutableStateOf("") }
    val options = remember(allOptions, selectedTag) { allOptions.withSelected(selectedTag) }
    val matches = remember(options, query) { options.matching(query) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(vertical = MaterialTheme.dimensions.space300)) {
            Text(
                text = stringResource(R.string.deck_language),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.space300),
            )
            Text(
                text = deckName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.space300),
            )
            Spacer(Modifier.height(MaterialTheme.dimensions.space200))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.deck_language_search_hint)) },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimensions.space300),
            )
            Spacer(Modifier.height(MaterialTheme.dimensions.space100))
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(LoadingHeight),
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LanguageList(
                    matches = matches,
                    selectedTag = selectedTag,
                    onLanguageSelected = onLanguageSelected,
                )
            }
            Spacer(Modifier.height(MaterialTheme.dimensions.space100))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimensions.space200),
            ) {
                TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
            }
        }
    }
}

@Composable
private fun LanguageList(
    matches: List<DeckLanguageOption>,
    selectedTag: String?,
    onLanguageSelected: (String?) -> Unit,
) {
    LazyColumn(modifier = Modifier.heightIn(max = MaxListHeight)) {
        item(key = NO_LANGUAGE_ITEM_KEY) {
            LanguageRow(
                label = stringResource(R.string.deck_language_none),
                selected = selectedTag == null,
                onClick = { onLanguageSelected(null) },
            )
        }
        items(matches, key = { it.tag }) { option ->
            LanguageRow(
                label = option.displayName,
                selected = option.tag == selectedTag,
                onClick = { onLanguageSelected(option.tag) },
            )
        }
        if (matches.isEmpty()) {
            item(key = NO_MATCHES_ITEM_KEY) {
                Text(
                    text = stringResource(R.string.deck_language_no_matches),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            horizontal = MaterialTheme.dimensions.space300,
                            vertical = MaterialTheme.dimensions.space200,
                        ),
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(
                    horizontal = MaterialTheme.dimensions.space300,
                    vertical = MaterialTheme.dimensions.space150,
                ),
    ) {
        // null onClick: the whole row is the touch target; a button of its own would be
        // announced as a second, separate control by TalkBack
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(MaterialTheme.dimensions.space200))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Stable [LazyColumn] key for the "no language" row; can't collide with a BCP 47 tag. */
private const val NO_LANGUAGE_ITEM_KEY = "deck-language-none"

/** Stable [LazyColumn] key for the empty-search message; can't collide with a BCP 47 tag. */
private const val NO_MATCHES_ITEM_KEY = "deck-language-no-matches"

@ThemePreviews
@Composable
private fun DeckLanguagePickerPreview(
    @PreviewParameter(SelectedTagProvider::class) selectedTag: String?,
) {
    AnkiDroidTheme {
        DeckLanguagePicker(
            deckName = "French::Verbs",
            selectedTag = selectedTag,
            isLoading = false,
            onLanguageSelected = {},
            onClose = {},
            allOptions =
                listOf(
                    DeckLanguageOption("de", "German"),
                    DeckLanguageOption("en", "English"),
                    DeckLanguageOption("fr", "French"),
                    DeckLanguageOption("ja", "Japanese"),
                ),
        )
    }
}

private class SelectedTagProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(null, "fr")
}
