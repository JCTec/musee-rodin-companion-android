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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Topic
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.SectionHeading

@Composable
fun PlacesScreen(
    contentRepository: ContentRepository,
    onOpenTopic: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    val places = listOf("topic-hotel-biron", "topic-garden", "topic-meudon")
        .mapNotNull(contentRepository::topic)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.PlacesView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("places.heroCard")
                    .semantics { contentDescription = "Personal companion, Musee Rodin Companion" },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("PERSONAL COMPANION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Musee Rodin Companion",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    "A private notebook and read-aloud guide. ${contentRepository.works.size} works catalogued and growing.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { SectionHeading("Places") }
        items(places, key = Topic::id) { topic ->
            PlaceRow(topic = topic, language = language, onClick = { onOpenTopic(topic.id) })
        }
    }
}

@Composable
private fun PlaceRow(topic: Topic, language: AppLanguage, onClick: () -> Unit) {
    val icon = when (topic.id) {
        "topic-garden" -> Icons.Filled.LocalFlorist
        "topic-meudon" -> Icons.Filled.Home
        else -> Icons.Filled.AccountBalance
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("place.row.${topic.id}")
            .semantics {
                role = Role.Button
                contentDescription = "${topic.title.value(language)}, ${topic.subtitle.value(language)}"
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(topic.title.value(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(topic.subtitle.value(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
