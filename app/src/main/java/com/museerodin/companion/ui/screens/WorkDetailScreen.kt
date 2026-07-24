package com.museerodin.companion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Work
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.CitationFlow
import com.museerodin.companion.ui.components.ConfidenceChip
import com.museerodin.companion.ui.components.FavoriteSeenActions
import com.museerodin.companion.ui.components.MetadataGrid
import com.museerodin.companion.ui.components.ReadAloudControls
import com.museerodin.companion.ui.components.SectionHeading
import com.museerodin.companion.ui.components.TagFlow
import com.museerodin.companion.ui.components.WorkArtwork
import com.museerodin.companion.user.FavoriteRecord
import com.museerodin.companion.user.SeenRecord
import com.museerodin.companion.user.UserLinkedKind
import com.museerodin.companion.user.UserRepository
import kotlinx.coroutines.launch

@Composable
fun WorkDetailScreen(
    work: Work,
    contentRepository: ContentRepository,
    favorites: List<FavoriteRecord>,
    seen: List<SeenRecord>,
    userRepository: UserRepository,
    narrationController: NarrationController,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    val scope = rememberCoroutineScope()
    var showResearch by remember { mutableStateOf(false) }
    var showSources by remember { mutableStateOf(false) }
    val isFavorite = favorites.any { it.linkedKindRaw == UserLinkedKind.WORK.rawValue && it.linkedID == work.id }
    val isSeen = seen.any { it.workID == work.id }
    val stop = contentRepository.audioStops.firstOrNull { it.linkedKind == ContentLinkKind.WORK && it.linkedID == work.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag(A11yTags.WorkDetailView)
            .padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WorkArtwork(work = work, language = language, hero = true)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    work.title.value(language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text("${work.artist} - ${work.dateText}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ConfidenceChip(work.confidence)
        }

        MetadataGrid(
            items = listOf(
                "Material" to work.material.value(language),
                "Date" to work.dateText,
                "Inventory" to (work.inventoryNumber ?: "source needed"),
                "Location" to work.locationStatus.value(language),
            ),
        )

        if (stop != null) {
            ReadAloudControls(
                stopID = stop.id,
                title = stop.title.value(language),
                narrationController = narrationController,
                onToggle = {
                    narrationController.toggle(stop, language)
                    val state = narrationController.state.value
                    scope.launch { userRepository.setPlaybackProgress(null, stop.id, state.rate.toDouble(), state.playbackState) }
                },
                onStop = {
                    narrationController.stop()
                    val state = narrationController.state.value
                    scope.launch { userRepository.setPlaybackProgress(null, stop.id, state.rate.toDouble(), state.playbackState) }
                },
            )
        }

        SectionHeading("About")
        Text(work.summary.value(language), style = MaterialTheme.typography.bodyLarge)

        ExpandableCard(
            title = "Research note",
            expanded = showResearch,
            onToggle = { showResearch = !showResearch },
            testTag = "work.research.toggle",
        ) {
            Text(work.researchNote.value(language), style = MaterialTheme.typography.bodyMedium)
        }

        ExpandableCard(
            title = "Sources",
            expanded = showSources,
            onToggle = { showSources = !showSources },
            testTag = "work.sources.toggle",
        ) {
            CitationFlow(citations = work.citations, contentRepository = contentRepository, language = language)
        }

        TagFlow(work.tags)

        FavoriteSeenActions(
            isFavorite = isFavorite,
            isSeen = isSeen,
            onFavorite = { scope.launch { userRepository.toggleFavorite(UserLinkedKind.WORK, work.id) } },
            onSeen = { scope.launch { userRepository.toggleSeen(work.id) } },
            onNote = onAddNote,
        )
    }
}

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().testTag(testTag),
            ) {
                Text(title, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
            if (expanded) {
                content()
            }
        }
    }
}

