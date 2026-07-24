package com.museerodin.companion.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Work
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.EmptyState
import com.museerodin.companion.ui.components.SectionHeading
import com.museerodin.companion.ui.components.WorkRow
import java.text.Normalizer
import java.util.Locale

private enum class WorkFilter(val label: String) {
    ALL("All"),
    RODIN("Rodin"),
    CLAUDEL("Claudel"),
    GARDEN("Garden"),
    MEUDON("Meudon"),
    DRAWING("Drawing"),
}

@Composable
fun WorksScreen(
    contentRepository: ContentRepository,
    onOpenWork: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(WorkFilter.ALL) }
    val works = remember(contentRepository, language, query, filter) {
        contentRepository.works.filter { work -> filter.matches(work) && work.matchesQuery(query, language) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.WorksView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(A11yTags.WorksSearchField)
                    .semantics { contentDescription = "Search works" },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("Titles, materials, notes") },
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkFilter.entries.forEach { candidate ->
                    FilterChip(
                        selected = filter == candidate,
                        onClick = { filter = candidate },
                        label = { Text(candidate.label) },
                        modifier = Modifier
                            .testTag("works.filter.${candidate.label}")
                            .semantics { stateDescription = if (filter == candidate) "selected" else "not selected" },
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionHeading("${works.size} of ${contentRepository.works.size} shown")
                Text("Use filters and search together to narrow the catalog.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (works.isEmpty()) {
            item {
                EmptyState("No works found", "Try a different search or filter.")
            }
        } else {
            items(works, key = Work::id) { work ->
                WorkRow(work = work, language = language, onClick = { onOpenWork(work.id) })
            }
        }
    }
}

private fun WorkFilter.matches(work: Work): Boolean = when (this) {
    WorkFilter.ALL -> true
    WorkFilter.RODIN -> work.artist.contains("Rodin", ignoreCase = true)
    WorkFilter.CLAUDEL -> work.artist.contains("Claudel", ignoreCase = true)
    WorkFilter.GARDEN -> work.tags.contains("garden")
    WorkFilter.MEUDON -> work.tags.contains("meudon")
    WorkFilter.DRAWING -> work.tags.contains("drawing")
}

private fun Work.matchesQuery(query: String, language: AppLanguage): Boolean {
    val needle = query.normalized()
    if (needle.isBlank()) return true
    return listOf(title.value(language), artist, material.value(language), summary.value(language), tags.joinToString(" "))
        .any { it.normalized().contains(needle) }
}

private fun String.normalized(): String {
    return Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
}

