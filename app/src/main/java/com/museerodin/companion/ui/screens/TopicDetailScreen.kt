package com.museerodin.companion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.content.Topic
import com.museerodin.companion.content.Work
import com.museerodin.companion.ui.A11yTags
import com.museerodin.companion.ui.components.CitationFlow
import com.museerodin.companion.ui.components.ConfidenceChip
import com.museerodin.companion.ui.components.PlaceholderPanel
import com.museerodin.companion.ui.components.SectionHeading
import com.museerodin.companion.ui.components.WorkRow

@Composable
fun TopicDetailScreen(
    topic: Topic,
    contentRepository: ContentRepository,
    onOpenWork: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = AppLanguage.fromLocale()
    val relatedWorks = topic.relatedWorkIDs.mapNotNull(contentRepository::work)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(A11yTags.TopicDetailView),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PlaceholderPanel(symbol = topic.placeholderSymbol, label = "topic placeholder")
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        topic.title.value(language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(topic.subtitle.value(language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ConfidenceChip(topic.confidence)
            }
        }
        item {
            Text(topic.summary.value(language), style = MaterialTheme.typography.bodyLarge)
        }
        item {
            SectionHeading("Research note")
            Text(topic.researchNote.value(language), modifier = Modifier.padding(top = 8.dp))
        }
        item { SectionHeading("Related works") }
        items(relatedWorks, key = Work::id) { work ->
            WorkRow(work = work, language = language, onClick = { onOpenWork(work.id) })
        }
        item {
            SectionHeading("Sources")
            CitationFlow(citations = topic.citations, contentRepository = contentRepository, language = language, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

