package com.museerodin.companion.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
enum class AppLanguage {
    @SerialName("en")
    EN,

    @SerialName("fr")
    FR,

    @SerialName("es")
    ES;

    val languageTag: String
        get() = when (this) {
            EN -> "en-US"
            FR -> "fr-FR"
            ES -> "es-MX"
        }

    companion object {
        fun fromLocale(locale: Locale = Locale.getDefault()): AppLanguage {
            return when (locale.language.lowercase(Locale.US)) {
                "fr" -> FR
                "es" -> ES
                else -> EN
            }
        }
    }
}

@Serializable
data class LocalizedText(
    val en: String,
    val fr: String,
    val es: String,
    val reviewNeeded: Boolean? = null,
) {
    fun value(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> en
        AppLanguage.FR -> fr
        AppLanguage.ES -> es
    }
}

@Serializable
enum class SourceKind {
    @SerialName("web")
    WEB,

    @SerialName("pdf")
    PDF,
}

@Serializable
enum class ContentConfidence {
    @SerialName("verified")
    VERIFIED,

    @SerialName("reviewNeeded")
    REVIEW_NEEDED,

    @SerialName("sourceNeeded")
    SOURCE_NEEDED,

    @SerialName("tertiary")
    TERTIARY,
}

@Serializable
enum class ContentLinkKind {
    @SerialName("work")
    WORK,

    @SerialName("topic")
    TOPIC,

    @SerialName("route")
    ROUTE,

    @SerialName("source")
    SOURCE,

    @SerialName("audioStop")
    AUDIO_STOP,
}

@Serializable
data class Citation(
    val id: String,
    val sourceID: String,
    val label: String,
    val page: Int? = null,
    val url: String? = null,
    val note: LocalizedText? = null,
)

@Serializable
data class Source(
    val id: String,
    val kind: SourceKind,
    val title: LocalizedText,
    val publisher: String,
    val url: String,
    val accessDate: String,
    val localFilename: String? = null,
    val notes: LocalizedText,
)

@Serializable
data class SourceChunk(
    val id: String,
    val sourceID: String,
    val page: Int? = null,
    val sectionHint: LocalizedText? = null,
    val text: LocalizedText,
    val citation: Citation,
)

@Serializable
data class Work(
    val id: String,
    val title: LocalizedText,
    val artist: String,
    val dateText: String,
    val material: LocalizedText,
    val inventoryNumber: String? = null,
    val locationStatus: LocalizedText,
    val summary: LocalizedText,
    val researchNote: LocalizedText,
    val confidence: ContentConfidence,
    val tags: List<String>,
    val citations: List<Citation>,
    val relatedTopicIDs: List<String>,
    val placeholderSymbol: String,
)

@Serializable
data class Topic(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val summary: LocalizedText,
    val researchNote: LocalizedText,
    val confidence: ContentConfidence,
    val tags: List<String>,
    val citations: List<Citation>,
    val relatedWorkIDs: List<String>,
    val placeholderSymbol: String,
)

@Serializable
data class Route(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val summary: LocalizedText,
    val estimatedMinutes: Int,
    val stopIDs: List<String>,
    val tags: List<String>,
    val citations: List<Citation>,
)

@Serializable
data class AudioStop(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val linkedKind: ContentLinkKind,
    val linkedID: String,
    val routeIDs: List<String>,
    val order: Int,
    val script: LocalizedText,
    val durationSecondsEstimate: Int,
    val citations: List<Citation>,
    val tags: List<String>,
)

data class ContentRepository(
    val sources: List<Source>,
    val sourceChunks: List<SourceChunk>,
    val works: List<Work>,
    val topics: List<Topic>,
    val routes: List<Route>,
    val audioStops: List<AudioStop>,
) {
    private val sourcesById = sources.associateBy(Source::id)
    private val worksById = works.associateBy(Work::id)
    private val topicsById = topics.associateBy(Topic::id)
    private val routesById = routes.associateBy(Route::id)
    private val audioStopsById = audioStops.associateBy(AudioStop::id)

    fun source(id: String): Source? = sourcesById[id]
    fun work(id: String): Work? = worksById[id]
    fun topic(id: String): Topic? = topicsById[id]
    fun route(id: String): Route? = routesById[id]
    fun audioStop(id: String): AudioStop? = audioStopsById[id]

    companion object {
        val Empty = ContentRepository(
            sources = emptyList(),
            sourceChunks = emptyList(),
            works = emptyList(),
            topics = emptyList(),
            routes = emptyList(),
            audioStops = emptyList(),
        )
    }
}

