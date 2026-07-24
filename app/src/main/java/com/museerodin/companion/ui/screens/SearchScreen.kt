package com.museerodin.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.search.SearchResult
import com.museerodin.companion.search.SearchResultKind
import com.museerodin.companion.search.SearchService
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.EmptyState
import com.museerodin.companion.user.ReadingNote

@Composable
fun SearchScreen(
    contentRepository: ContentRepository,
    notes: List<ReadingNote>,
    onOpenResult: (ContentLinkKind, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    var query by remember { mutableStateOf("") }
    val results = remember(query, contentRepository, notes, language) {
        SearchService.search(query, contentRepository, notes, language)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.SearchView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(A11yTags.SearchField)
                    .semantics { contentDescription = "Search works, topics, paths, sources, notes" },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("Works, topics, paths, sources, notes") },
            )
        }
        when {
            query.isBlank() -> item {
                EmptyState("Search the app", "Search works, topics, paths, source chunks, and your notes.")
            }

            results.isEmpty() -> item {
                EmptyState("No results", "No matches for \"$query\".")
            }

            else -> {
                SearchResultKind.entries.forEach { kind ->
                    val grouped = results.filter { it.kind == kind }
                    if (grouped.isNotEmpty()) {
                        item {
                            Text(
                                kind.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        items(grouped, key = SearchResult::id) { result ->
                            SearchResultRow(result = result, onOpenResult = onOpenResult)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    onOpenResult: (ContentLinkKind, String) -> Unit,
) {
    val clickable = result.linkedKind != null && result.linkedID != null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search.result.${result.id}")
            .semantics {
                contentDescription = "${result.kind.label}, ${result.title}, ${result.subtitle}"
                if (clickable) role = Role.Button
            }
            .then(
                if (clickable) {
                    Modifier.clickable {
                        onOpenResult(result.linkedKind!!, result.linkedID!!)
                    }
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(result.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(result.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(result.snippet, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val SearchResultKind.label: String
    get() = when (this) {
        SearchResultKind.WORK -> "Works"
        SearchResultKind.TOPIC -> "Topics"
        SearchResultKind.ROUTE -> "Paths"
        SearchResultKind.SOURCE_CHUNK -> "Sources"
        SearchResultKind.NOTE -> "Notes"
    }

