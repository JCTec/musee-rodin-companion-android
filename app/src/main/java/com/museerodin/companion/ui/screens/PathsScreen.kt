package com.museerodin.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.AudioStop
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Route
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.CitationFlow
import com.museerodin.companion.ui.components.EmptyState
import com.museerodin.companion.ui.components.ReadAloudControls
import com.museerodin.companion.ui.components.SectionHeading
import com.museerodin.companion.user.UserRepository
import kotlinx.coroutines.launch

@Composable
fun PathsScreen(
    contentRepository: ContentRepository,
    onOpenPath: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.PathsView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Symbolic paths connect works, places, and themes. Each stop can be read aloud and opened in context.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { SectionHeading("Paths") }
        if (contentRepository.routes.isEmpty()) {
            item { EmptyState("No paths", "Bundled route content is missing.") }
        } else {
            items(contentRepository.routes, key = Route::id) { route ->
                PathRow(route = route, language = language, onClick = { onOpenPath(route.id) })
            }
        }
    }
}

@Composable
private fun PathRow(route: Route, language: AppLanguage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("path.row.${route.id}")
            .semantics {
                role = Role.Button
                contentDescription = "${route.title.value(language)}, ${route.stopIDs.size} stops"
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(route.title.value(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(route.subtitle.value(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.List, contentDescription = null)
                Text("${route.stopIDs.size} stops", style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Filled.Schedule, contentDescription = null)
                Text("${route.estimatedMinutes} min", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun PathDetailScreen(
    route: Route,
    contentRepository: ContentRepository,
    userRepository: UserRepository,
    narrationController: NarrationController,
    onOpenLinkedItem: (ContentLinkKind, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    val stops = route.stopIDs.mapNotNull(contentRepository::audioStop)
    val progress by userRepository.routeProgress(route.id).collectAsStateWithLifecycle(initialValue = null)
    val completed = progress?.completedStopIDs.orEmpty()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.PathDetailView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    route.title.value(language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(route.summary.value(language), style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${stops.size} stops", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${route.estimatedMinutes} min", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(
                    onClick = {
                        narrationController.stop()
                        val state = narrationController.state.value
                        scope.launch { userRepository.setPlaybackProgress(route.id, state.currentStopID, state.rate.toDouble(), state.playbackState) }
                    },
                    modifier = Modifier.testTag(A11yTags.ReadAloudStopButton),
                ) {
                    Text("Stop read aloud")
                }
            }
        }
        items(stops.size, key = { stops[it].id }) { index ->
            val stop = stops[index]
            StopCard(
                index = index,
                stop = stop,
                nextStop = stops.getOrNull(index + 1),
                route = route,
                completed = completed.contains(stop.id),
                contentRepository = contentRepository,
                language = language,
                narrationController = narrationController,
                onOpenLinkedItem = onOpenLinkedItem,
                onReadAloud = {
                    narrationController.toggle(stop, language)
                    val state = narrationController.state.value
                    scope.launch {
                        userRepository.setRouteProgress(route.id, stop.id, completed)
                        userRepository.setPlaybackProgress(route.id, stop.id, state.rate.toDouble(), state.playbackState)
                    }
                },
                onStop = {
                    narrationController.stop()
                    val state = narrationController.state.value
                    scope.launch { userRepository.setPlaybackProgress(route.id, stop.id, state.rate.toDouble(), state.playbackState) }
                },
                onToggleComplete = {
                    val updated = if (completed.contains(stop.id)) completed - stop.id else completed + stop.id
                    scope.launch { userRepository.setRouteProgress(route.id, stop.id, updated) }
                },
            )
        }
    }
}

@Composable
private fun StopCard(
    index: Int,
    stop: AudioStop,
    nextStop: AudioStop?,
    route: Route,
    completed: Boolean,
    contentRepository: ContentRepository,
    language: AppLanguage,
    narrationController: NarrationController,
    onReadAloud: () -> Unit,
    onStop: () -> Unit,
    onOpenLinkedItem: (ContentLinkKind, String) -> Unit,
    onToggleComplete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("path.stop.${stop.id}")
            .semantics { contentDescription = "Stop ${index + 1}, ${stop.title.value(language)}" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Stop ${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("path.stop.number.${stop.id}"),
            )
            Text(stop.title.value(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stop.subtitle.value(language), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stop.script.value(language), style = MaterialTheme.typography.bodyMedium)

            ReadAloudControls(
                stopID = stop.id,
                title = stop.title.value(language),
                narrationController = narrationController,
                onToggle = onReadAloud,
                onStop = onStop,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onOpenLinkedItem(stop.linkedKind, stop.linkedID) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("path.stop.itemButton.${stop.id}")
                        .semantics {
                            contentDescription = linkedItemAccessibilityLabel(stop.linkedKind)
                            role = Role.Button
                        },
                ) {
                    Icon(linkedItemIcon(stop.linkedKind), contentDescription = null)
                    Text(linkedItemTitle(stop, contentRepository, language), modifier = Modifier.padding(start = 8.dp))
                }
                FilterChip(
                    selected = completed,
                    onClick = onToggleComplete,
                    label = { Text(if (completed) "Done" else "Mark done") },
                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    modifier = Modifier
                        .testTag("path.stop.done.${stop.id}")
                        .semantics { stateDescription = if (completed) "completed" else "not completed" },
                )
            }

            if (nextStop != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("path.stop.nextStop.${stop.id}")
                        .semantics { contentDescription = "Next stop, ${nextStop.title.value(language)}" },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("Next stop", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nextStop.title.value(language), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (stop.citations.isNotEmpty()) {
                Column(Modifier.testTag("path.stop.sources.${stop.id}"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Sources")
                    CitationFlow(citations = stop.citations, contentRepository = contentRepository, language = language)
                }
            }
        }
    }
}

private fun linkedItemTitle(stop: AudioStop, contentRepository: ContentRepository, language: AppLanguage): String {
    return when (stop.linkedKind) {
        ContentLinkKind.WORK -> contentRepository.work(stop.linkedID)?.title?.value(language)?.let { "Open work: $it" }
        ContentLinkKind.TOPIC -> contentRepository.topic(stop.linkedID)?.title?.value(language)?.let { "Open item: $it" }
        ContentLinkKind.ROUTE -> contentRepository.route(stop.linkedID)?.title?.value(language)?.let { "Open path: $it" }
        ContentLinkKind.SOURCE,
        ContentLinkKind.AUDIO_STOP,
        -> null
    } ?: "Open linked item"
}

private fun linkedItemAccessibilityLabel(kind: ContentLinkKind): String {
    return when (kind) {
        ContentLinkKind.WORK -> "Open related work"
        ContentLinkKind.TOPIC -> "Open related item"
        ContentLinkKind.ROUTE -> "Open related path"
        ContentLinkKind.SOURCE,
        ContentLinkKind.AUDIO_STOP,
        -> "Open linked item"
    }
}

private fun linkedItemIcon(kind: ContentLinkKind) = when (kind) {
    ContentLinkKind.WORK -> Icons.Filled.Star
    ContentLinkKind.TOPIC -> Icons.Filled.Map
    ContentLinkKind.ROUTE -> Icons.Filled.Map
    ContentLinkKind.SOURCE,
    ContentLinkKind.AUDIO_STOP,
    -> Icons.Filled.Map
}
