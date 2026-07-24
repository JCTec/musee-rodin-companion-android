package com.museerodin.companion.ui.components

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.Citation
import com.museerodin.companion.content.ContentConfidence
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Work
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.user.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PlaceholderPanel(
    symbol: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
            Text(symbol, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WorkArtwork(
    work: Work,
    language: AppLanguage,
    hero: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(
        Color(0xFF7B5427),
        Color(0xFF1F7A68),
        Color(0xFF704565),
        Color(0xFF8A5A44),
        Color(0xFF3E6D8E),
    )
    val color = colors[work.id.hashCode().absoluteValue % colors.size]
    val title = work.title.value(language)
    val image = rememberWorkImageBitmap("work-images/${work.id}.jpg")
    var showFullScreen by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .then(if (hero) Modifier.fillMaxWidth().height(260.dp) else Modifier.size(56.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (hero) 0.24f else 0.18f))
            .testTag("work.image.${work.id}")
            .then(
                if (image != null) {
                    Modifier.clickable { showFullScreen = true }
                } else {
                    Modifier
                },
            )
            .semantics {
                contentDescription = if (image != null) {
                    "Open full screen artwork image for $title"
                } else {
                    "Artwork placeholder for $title. Image slot ${work.id}"
                }
                if (image != null) role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(if (hero) 42.dp else 22.dp),
                )
                if (hero) {
                    Text(work.id, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = color)
                    Text("Image slot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (image != null && showFullScreen) {
        FullScreenArtworkDialog(
            image = image,
            title = title,
            workID = work.id,
            onDismiss = { showFullScreen = false },
        )
    }
}

@Composable
private fun rememberWorkImageBitmap(assetPath: String): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(null, assetPath, context) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(assetPath).use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    return image
}

@Composable
private fun FullScreenArtworkDialog(
    image: ImageBitmap,
    title: String,
    workID: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("work.image.fullscreen.$workID")
                .semantics { contentDescription = "Full screen artwork image for $title" },
        ) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit,
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .testTag("work.image.fullscreen.close.$workID")
                    .semantics { contentDescription = "Close full screen artwork image" },
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun WorkRow(
    work: Work,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work.row.${work.id}")
            .semantics {
                role = Role.Button
                contentDescription = "${work.title.value(language)}, ${work.artist}, ${work.dateText}"
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WorkArtwork(work = work, language = language, hero = false)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(work.title.value(language), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${work.artist} - ${work.dateText}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(work.material.value(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ConfidenceChip(confidence: ContentConfidence, modifier: Modifier = Modifier) {
    val (label, icon) = when (confidence) {
        ContentConfidence.VERIFIED -> "Verified" to Icons.Filled.Verified
        ContentConfidence.REVIEW_NEEDED -> "Review needed" to Icons.Filled.ErrorOutline
        ContentConfidence.SOURCE_NEEDED -> "Source needed" to Icons.Filled.Help
        ContentConfidence.TERTIARY -> "Tertiary source" to Icons.Filled.Schedule
    }
    ElevatedAssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = modifier
            .testTag("confidence.$label")
            .semantics { contentDescription = label },
    )
}

@Composable
fun TagChip(title: String, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = {},
        label = { Text(title) },
        modifier = modifier
            .testTag("tag.$title")
            .semantics { contentDescription = title },
    )
}

@Composable
fun CitationChip(
    citation: Citation,
    contentRepository: ContentRepository,
    language: AppLanguage,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val source = contentRepository.source(citation.sourceID)
    val url = citation.url ?: source?.url
    val displayTitle = when {
        citation.page != null -> "PDF p.${citation.page}"
        source?.kind?.name == "PDF" -> "PDF source"
        else -> "Official page"
    }
    val sourceTitle = source?.title?.value(language) ?: displayTitle
    AssistChip(
        onClick = {
            if (url != null) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        },
        enabled = url != null,
        label = { Text(displayTitle) },
        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = modifier
            .testTag("citation.${citation.id}")
            .semantics {
                contentDescription = if (citation.page != null) {
                    "Open PDF source, $sourceTitle, page ${citation.page}"
                } else {
                    "Open $displayTitle, $sourceTitle"
                }
            },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitationFlow(
    citations: List<Citation>,
    contentRepository: ContentRepository,
    language: AppLanguage,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        citations.forEach { citation ->
            CitationChip(citation = citation, contentRepository = contentRepository, language = language)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagFlow(tags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag -> TagChip(title = tag) }
    }
}

@Composable
fun MetadataGrid(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metadata.grid")
            .semantics { contentDescription = items.joinToString { "${it.first}: ${it.second}" } },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(96.dp),
                    )
                    Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ReadAloudControls(
    stopID: String,
    title: String,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    narrationController: NarrationController,
    modifier: Modifier = Modifier,
) {
    val state by narrationController.state.collectAsStateWithLifecycle()
    val isCurrent = state.currentStopID == stopID
    val playbackState = if (isCurrent) state.playbackState else PlaybackState.IDLE
    val label = when (playbackState) {
        PlaybackState.SPEAKING -> "Pause"
        PlaybackState.PAUSED -> "Resume"
        else -> "Read aloud"
    }
    val icon = when (playbackState) {
        PlaybackState.SPEAKING -> Icons.Filled.Pause
        else -> Icons.Filled.PlayArrow
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onToggle,
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .testTag(A11yTags.ReadAloudButton)
                .semantics {
                    contentDescription = "$label $title"
                    stateDescription = playbackState.rawValue
                },
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
        IconButton(
            onClick = {
                val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                val currentIndex = speeds.indexOfFirst { kotlin.math.abs(it - state.rate) < 0.01f }.coerceAtLeast(0)
                narrationController.rate = speeds[(currentIndex + 1) % speeds.size]
            },
            modifier = Modifier
                .size(48.dp)
                .testTag(A11yTags.ReadAloudSpeedButton)
                .semantics {
                    contentDescription = "Change read aloud speed"
                    stateDescription = "${state.rate}x"
                },
        ) {
            Text("${state.rate}x", style = MaterialTheme.typography.labelMedium)
        }
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(48.dp)
                .testTag(A11yTags.ReadAloudStopButton)
                .semantics {
                    contentDescription = "Stop read aloud"
                    stateDescription = playbackState.rawValue
                },
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
        }
    }
}

@Composable
fun ToggleActionButton(
    selected: Boolean,
    selectedLabel: String,
    unselectedLabel: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(if (selected) selectedLabel else unselectedLabel) },
        leadingIcon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        modifier = modifier
            .height(48.dp)
            .testTag(testTag)
            .semantics {
                contentDescription = if (selected) selectedLabel else unselectedLabel
                stateDescription = if (selected) "selected" else "not selected"
            },
    )
}

@Composable
fun FavoriteSeenActions(
    isFavorite: Boolean,
    isSeen: Boolean,
    onFavorite: () -> Unit,
    onSeen: () -> Unit,
    onNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleActionButton(
            selected = isFavorite,
            selectedLabel = "Favorited",
            unselectedLabel = "Favorite",
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Filled.Favorite,
            testTag = A11yTags.FavoriteButton,
            onClick = onFavorite,
        )
        ToggleActionButton(
            selected = isSeen,
            selectedLabel = "Seen",
            unselectedLabel = "Seen",
            selectedIcon = Icons.Filled.CheckCircle,
            unselectedIcon = Icons.Filled.CheckCircle,
            testTag = A11yTags.SeenButton,
            onClick = onSeen,
        )
        OutlinedButton(
            onClick = onNote,
            modifier = Modifier
                .height(48.dp)
                .testTag(A11yTags.AddNoteButton)
                .semantics { contentDescription = "Add note" },
        ) {
            Text("Note")
        }
    }
}
