package com.museerodin.companion.search

import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.user.ReadingNote
import java.text.Normalizer
import java.util.Locale

enum class SearchResultKind {
    WORK,
    TOPIC,
    ROUTE,
    SOURCE_CHUNK,
    NOTE,
}

data class SearchResult(
    val id: String,
    val kind: SearchResultKind,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val linkedKind: ContentLinkKind? = null,
    val linkedID: String? = null,
)

object SearchService {
    fun search(
        query: String,
        content: ContentRepository,
        notes: List<ReadingNote>,
        language: AppLanguage,
    ): List<SearchResult> {
        val needle = query.normalized()
        if (needle.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()

        content.works.forEach { work ->
            if (matches(needle, work.title.value(language), work.artist, work.summary.value(language), work.material.value(language), work.tags.joinToString(" "))) {
                results += SearchResult(
                    id = "work-${work.id}",
                    kind = SearchResultKind.WORK,
                    title = work.title.value(language),
                    subtitle = work.artist,
                    snippet = work.summary.value(language),
                    linkedKind = ContentLinkKind.WORK,
                    linkedID = work.id,
                )
            }
        }

        content.topics.forEach { topic ->
            if (matches(needle, topic.title.value(language), topic.subtitle.value(language), topic.summary.value(language), topic.tags.joinToString(" "))) {
                results += SearchResult(
                    id = "topic-${topic.id}",
                    kind = SearchResultKind.TOPIC,
                    title = topic.title.value(language),
                    subtitle = topic.subtitle.value(language),
                    snippet = topic.summary.value(language),
                    linkedKind = ContentLinkKind.TOPIC,
                    linkedID = topic.id,
                )
            }
        }

        content.routes.forEach { route ->
            if (matches(needle, route.title.value(language), route.subtitle.value(language), route.summary.value(language), route.tags.joinToString(" "))) {
                results += SearchResult(
                    id = "route-${route.id}",
                    kind = SearchResultKind.ROUTE,
                    title = route.title.value(language),
                    subtitle = route.subtitle.value(language),
                    snippet = route.summary.value(language),
                    linkedKind = ContentLinkKind.ROUTE,
                    linkedID = route.id,
                )
            }
        }

        content.sourceChunks.forEach { chunk ->
            val source = content.source(chunk.sourceID)
            if (matches(needle, source?.title?.value(language).orEmpty(), chunk.sectionHint?.value(language).orEmpty(), chunk.text.value(language))) {
                results += SearchResult(
                    id = "source-chunk-${chunk.id}",
                    kind = SearchResultKind.SOURCE_CHUNK,
                    title = source?.title?.value(language) ?: chunk.citation.label,
                    subtitle = chunk.sectionHint?.value(language) ?: chunk.citation.label,
                    snippet = chunk.text.value(language),
                    linkedKind = null,
                    linkedID = null,
                )
            }
        }

        notes.forEach { note ->
            if (matches(needle, note.title, note.body, note.linkedKindRaw, note.linkedID)) {
                results += SearchResult(
                    id = "note-${note.id}",
                    kind = SearchResultKind.NOTE,
                    title = note.title,
                    subtitle = note.linkedKindRaw,
                    snippet = note.body,
                )
            }
        }

        return results
    }

    private fun matches(needle: String, vararg values: String): Boolean {
        return values.any { it.normalized().contains(needle) }
    }

    private fun String.normalized(): String {
        val withoutMarks = Normalizer.normalize(trim(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return withoutMarks.lowercase(Locale.ROOT)
    }
}

