package com.museerodin.companion.content

import android.content.res.AssetManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
object ContentJson {
    val format: Json = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }
}

class ContentAssetDataSource(private val assets: AssetManager) {
    fun load(): ContentRepository {
        return ContentRepository(
            sources = decodeAsset("sources.json", Source.serializer()),
            sourceChunks = decodeAsset("source_chunks.json", SourceChunk.serializer()),
            works = decodeAsset("works.json", Work.serializer()),
            topics = decodeAsset("topics.json", Topic.serializer()),
            routes = decodeAsset("routes.json", Route.serializer()),
            audioStops = decodeAsset("audio_stops.json", AudioStop.serializer()),
        )
    }

    private fun <T> decodeAsset(fileName: String, serializer: kotlinx.serialization.KSerializer<T>): List<T> {
        val json = assets.open("content/$fileName").bufferedReader().use { it.readText() }
        return ContentJson.format.decodeFromString(ListSerializer(serializer), json)
    }
}

object ContentFileDataSource {
    fun load(directory: File): ContentRepository {
        return ContentRepository(
            sources = decodeFile(directory, "sources.json", Source.serializer()),
            sourceChunks = decodeFile(directory, "source_chunks.json", SourceChunk.serializer()),
            works = decodeFile(directory, "works.json", Work.serializer()),
            topics = decodeFile(directory, "topics.json", Topic.serializer()),
            routes = decodeFile(directory, "routes.json", Route.serializer()),
            audioStops = decodeFile(directory, "audio_stops.json", AudioStop.serializer()),
        )
    }

    private fun <T> decodeFile(
        directory: File,
        fileName: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): List<T> {
        val json = directory.resolve(fileName).readText()
        return ContentJson.format.decodeFromString(ListSerializer(serializer), json)
    }
}

