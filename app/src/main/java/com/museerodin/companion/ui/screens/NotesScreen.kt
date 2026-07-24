package com.museerodin.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.EmptyState
import com.museerodin.companion.ui.components.SectionHeading
import com.museerodin.companion.ui.components.WorkRow
import com.museerodin.companion.user.FavoriteRecord
import com.museerodin.companion.user.ReadingNote
import com.museerodin.companion.user.SeenRecord
import com.museerodin.companion.user.UserLinkedKind
import com.museerodin.companion.user.UserRepository
import kotlinx.coroutines.launch

private enum class NotesSegment(val label: String) {
    NOTES("Notes"),
    FAVORITES("Favorites"),
    SEEN("Seen"),
}

@Composable
fun NotesScreen(
    contentRepository: ContentRepository,
    notes: List<ReadingNote>,
    favorites: List<FavoriteRecord>,
    seen: List<SeenRecord>,
    userRepository: UserRepository,
    onOpenWork: (String) -> Unit,
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    val scope = rememberCoroutineScope()
    var segment by remember { mutableStateOf(NotesSegment.NOTES) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.NotesView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotesSegment.entries.forEach { candidate ->
                    FilterChip(
                        selected = segment == candidate,
                        onClick = { segment = candidate },
                        label = { Text(candidate.label) },
                        modifier = Modifier
                            .testTag("notes.segment.${candidate.label}")
                            .semantics { stateDescription = if (segment == candidate) "selected" else "not selected" },
                    )
                }
            }
        }
        item {
            Button(
                onClick = onAddNote,
                modifier = Modifier
                    .testTag(A11yTags.AddNoteButton)
                    .semantics { contentDescription = "Add note" },
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add note", modifier = Modifier.padding(start = 8.dp))
            }
        }

        when (segment) {
            NotesSegment.NOTES -> {
                if (notes.isEmpty()) {
                    item { EmptyState("No notes yet", "Notes you add on any work or place appear here.") }
                } else {
                    item { SectionHeading("Notes") }
                    items(notes, key = ReadingNote::id) { note ->
                        NoteRow(
                            note = note,
                            linkedTitle = linkedItemLabel(note, contentRepository, language),
                            onEdit = { onEditNote(note.id) },
                            onDelete = { scope.launch { userRepository.deleteNote(note.id) } },
                        )
                    }
                }
            }

            NotesSegment.FAVORITES -> {
                val favoriteWorks = favorites.mapNotNull { contentRepository.work(it.linkedID) }
                if (favoriteWorks.isEmpty()) {
                    item { EmptyState("No favorites", "Favorite works to find them here.") }
                } else {
                    item { SectionHeading("Favorites") }
                    items(favoriteWorks, key = { it.id }) { work ->
                        WorkRow(work = work, language = language, onClick = { onOpenWork(work.id) })
                    }
                }
            }

            NotesSegment.SEEN -> {
                val seenWorks = seen.mapNotNull { contentRepository.work(it.workID) }
                if (seenWorks.isEmpty()) {
                    item { EmptyState("Nothing marked seen", "Mark works as seen from their detail page.") }
                } else {
                    item { SectionHeading("Seen") }
                    items(seenWorks, key = { it.id }) { work ->
                        WorkRow(work = work, language = language, onClick = { onOpenWork(work.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: ReadingNote,
    linkedTitle: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note.row.${note.id}")
            .clickable(onClick = onEdit)
            .semantics {
                role = Role.Button
                contentDescription = "Note ${note.title}, linked to $linkedTitle"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(linkedTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(note.body, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit, modifier = Modifier.testTag("note.editButton")) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Text("Edit", modifier = Modifier.padding(start = 4.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .testTag(A11yTags.DeleteNoteButton)
                        .semantics { contentDescription = "Delete note ${note.title}" },
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun NoteEditorScreen(
    noteID: String?,
    linkedKind: UserLinkedKind,
    linkedID: String,
    suggestedTitle: String,
    notes: List<ReadingNote>,
    userRepository: UserRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val existing = notes.firstOrNull { it.id == noteID }
    var title by remember(noteID) { mutableStateOf(existing?.title ?: suggestedTitle) }
    var body by remember(noteID) { mutableStateOf(existing?.body.orEmpty()) }

    LaunchedEffect(existing?.id) {
        if (existing != null) {
            title = existing.title
            body = existing.body
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.NoteEditorView)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(if (existing == null) "New note" else "Edit note", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Linked item: ${existing?.linkedKindRaw ?: linkedKind.rawValue} / ${existing?.linkedID ?: linkedID}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note.titleField")
                .semantics { contentDescription = "Note title" },
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .testTag("note.bodyField")
                .semantics { contentDescription = "Note body" },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
            if (existing != null) {
                TextButton(
                    onClick = {
                        scope.launch {
                            userRepository.deleteNote(existing.id)
                            onClose()
                        }
                    },
                    modifier = Modifier.testTag(A11yTags.DeleteNoteButton),
                ) {
                    Text("Delete")
                }
            }
            Button(
                onClick = {
                    val savedTitle = title
                    val savedBody = body
                    val savedExisting = existing
                    scope.launch {
                        if (savedExisting == null) {
                            userRepository.createNote(linkedKind, linkedID, savedTitle, savedBody)
                        } else {
                            userRepository.updateNote(savedExisting.id, savedTitle, savedBody)
                        }
                    }
                    onClose()
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.testTag(A11yTags.SaveNoteButton),
            ) {
                Text("Save")
            }
        }
    }
}

private fun linkedItemLabel(note: ReadingNote, contentRepository: ContentRepository, language: AppLanguage): String {
    return when (note.linkedKindRaw) {
        UserLinkedKind.WORK.rawValue -> contentRepository.work(note.linkedID)?.title?.value(language)?.let { "Work: $it" }
        UserLinkedKind.TOPIC.rawValue -> contentRepository.topic(note.linkedID)?.title?.value(language)?.let { "Topic: $it" }
        UserLinkedKind.ROUTE.rawValue -> contentRepository.route(note.linkedID)?.title?.value(language)?.let { "Path: $it" }
        UserLinkedKind.SOURCE.rawValue -> contentRepository.source(note.linkedID)?.title?.value(language)?.let { "Source: $it" }
        else -> null
    } ?: "${note.linkedKindRaw}: ${note.linkedID}"
}
