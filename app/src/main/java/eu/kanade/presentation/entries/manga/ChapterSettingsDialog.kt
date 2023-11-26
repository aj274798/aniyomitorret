package eu.kanade.presentation.entries.manga

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.kanade.domain.entries.manga.model.downloadedFilter
import eu.kanade.domain.entries.manga.model.forceDownloaded
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.R
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.core.preference.TriState
import tachiyomi.domain.entries.manga.model.Manga
import tachiyomi.presentation.core.components.LabeledCheckbox
import tachiyomi.presentation.core.components.RadioItem
import tachiyomi.presentation.core.components.SortItem
import tachiyomi.presentation.core.components.TriStateItem
import tachiyomi.presentation.core.theme.active

@Composable
fun ChapterSettingsDialog(
    onDismissRequest: () -> Unit,
    manga: Manga? = null,
    onDownloadFilterChanged: (TriState) -> Unit,
    onUnreadFilterChanged: (TriState) -> Unit,
    onBookmarkedFilterChanged: (TriState) -> Unit,
    scanlatorFilterActive: Boolean,
    onScanlatorFilterClicked: (() -> Unit),
    onSortModeChanged: (Long) -> Unit,
    onDisplayModeChanged: (Long) -> Unit,
    onSetAsDefault: (applyToExistingManga: Boolean) -> Unit,
    onResetToDefault: () -> Unit,
) {
    var showSetAsDefaultDialog by rememberSaveable { mutableStateOf(false) }
    if (showSetAsDefaultDialog) {
        SetAsDefaultDialog(
            onDismissRequest = { showSetAsDefaultDialog = false },
            onConfirmed = onSetAsDefault,
        )
    }

    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = persistentListOf(
            stringResource(R.string.action_filter),
            stringResource(R.string.action_sort),
            stringResource(R.string.action_display),
        ),
        tabOverflowMenuContent = { closeMenu ->
            DropdownMenuItem(
                text = { Text(stringResource(R.string.set_chapter_settings_as_default)) },
                onClick = {
                    showSetAsDefaultDialog = true
                    closeMenu()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_reset)) },
                onClick = {
                    onResetToDefault()
                    closeMenu()
                },
            )
        },
    ) { page ->
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> {
                    FilterPage(
                        downloadFilter = manga?.downloadedFilter ?: TriState.DISABLED,
                        onDownloadFilterChanged = onDownloadFilterChanged
                            .takeUnless { manga?.forceDownloaded() == true },
                        unreadFilter = manga?.unreadFilter ?: TriState.DISABLED,
                        onUnreadFilterChanged = onUnreadFilterChanged,
                        bookmarkedFilter = manga?.bookmarkedFilter ?: TriState.DISABLED,
                        onBookmarkedFilterChanged = onBookmarkedFilterChanged,
                        scanlatorFilterActive = scanlatorFilterActive,
                        onScanlatorFilterClicked = onScanlatorFilterClicked,
                    )
                }
                1 -> {
                    SortPage(
                        sortingMode = manga?.sorting ?: 0,
                        sortDescending = manga?.sortDescending() ?: false,
                        onItemSelected = onSortModeChanged,
                    )
                }
                2 -> {
                    DisplayPage(
                        displayMode = manga?.displayMode ?: 0,
                        onItemSelected = onDisplayModeChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPage(
    downloadFilter: TriState,
    onDownloadFilterChanged: ((TriState) -> Unit)?,
    unreadFilter: TriState,
    onUnreadFilterChanged: (TriState) -> Unit,
    bookmarkedFilter: TriState,
    onBookmarkedFilterChanged: (TriState) -> Unit,
    scanlatorFilterActive: Boolean,
    onScanlatorFilterClicked: (() -> Unit),
) {
    TriStateItem(
        label = stringResource(R.string.label_downloaded),
        state = downloadFilter,
        onClick = onDownloadFilterChanged,
    )
    TriStateItem(
        label = stringResource(R.string.action_filter_unread),
        state = unreadFilter,
        onClick = onUnreadFilterChanged,
    )
    TriStateItem(
        label = stringResource(R.string.action_filter_bookmarked),
        state = bookmarkedFilter,
        onClick = onBookmarkedFilterChanged,
    )
    ScanlatorFilterItem(
        active = scanlatorFilterActive,
        onClick = onScanlatorFilterClicked,
    )
}

@Composable
fun ScanlatorFilterItem(
    active: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = TabbedDialogPaddings.Horizontal, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.PeopleAlt,
            contentDescription = null,
            tint = if (active) {
                MaterialTheme.colorScheme.active
            } else {
                LocalContentColor.current
            },
        )
        Text(
            text = stringResource(R.string.scanlator),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SortPage(
    sortingMode: Long,
    sortDescending: Boolean,
    onItemSelected: (Long) -> Unit,
) {
    SortItem(
        label = stringResource(R.string.sort_by_source),
        sortDescending = sortDescending.takeIf { sortingMode == Manga.CHAPTER_SORTING_SOURCE },
        onClick = { onItemSelected(Manga.CHAPTER_SORTING_SOURCE) },
    )
    SortItem(
        label = stringResource(R.string.sort_by_number),
        sortDescending = sortDescending.takeIf { sortingMode == Manga.CHAPTER_SORTING_NUMBER },
        onClick = { onItemSelected(Manga.CHAPTER_SORTING_NUMBER) },
    )
    SortItem(
        label = stringResource(R.string.sort_by_upload_date),
        sortDescending = sortDescending.takeIf { sortingMode == Manga.CHAPTER_SORTING_UPLOAD_DATE },
        onClick = { onItemSelected(Manga.CHAPTER_SORTING_UPLOAD_DATE) },
    )
    SortItem(
        label = stringResource(R.string.action_sort_alpha),
        sortDescending = sortDescending.takeIf { sortingMode == Manga.CHAPTER_SORTING_ALPHABET },
        onClick = { onItemSelected(Manga.CHAPTER_SORTING_ALPHABET) },
    )
}

@Composable
private fun DisplayPage(
    displayMode: Long,
    onItemSelected: (Long) -> Unit,
) {
    RadioItem(
        label = stringResource(R.string.show_title),
        selected = displayMode == Manga.CHAPTER_DISPLAY_NAME,
        onClick = { onItemSelected(Manga.CHAPTER_DISPLAY_NAME) },
    )
    RadioItem(
        label = stringResource(R.string.show_chapter_number),
        selected = displayMode == Manga.CHAPTER_DISPLAY_NUMBER,
        onClick = { onItemSelected(Manga.CHAPTER_DISPLAY_NUMBER) },
    )
}

@Composable
private fun SetAsDefaultDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: (optionalChecked: Boolean) -> Unit,
) {
    var optionalChecked by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.chapter_settings)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = stringResource(R.string.confirm_set_chapter_settings))

                LabeledCheckbox(
                    label = stringResource(R.string.also_set_chapter_settings_for_library),
                    checked = optionalChecked,
                    onCheckedChange = { optionalChecked = it },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmed(optionalChecked)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
    )
}
