package com.museerodin.companion

import com.museerodin.companion.content.ContentFileDataSource
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentDecodingTest {
    private val repo by lazy {
        ContentFileDataSource.load(File("src/main/assets/content"))
    }

    @Test
    fun decodesBundledContent() {
        assertEquals(47, repo.sources.size)
        assertEquals(28, repo.sourceChunks.size)
        assertEquals(18, repo.works.size)
        assertEquals(16, repo.topics.size)
        assertEquals(12, repo.routes.size)
        assertEquals(29, repo.audioStops.size)
    }

    @Test
    fun contentCitationCoverageMatchesSchemaRule() {
        repo.works.forEach { work ->
            assertTrue("${work.id} should have citations", work.citations.isNotEmpty())
            work.citations.forEach { assertNotNull(repo.source(it.sourceID)) }
        }
        repo.topics.forEach { topic ->
            assertTrue("${topic.id} should have citations", topic.citations.isNotEmpty())
            topic.citations.forEach { assertNotNull(repo.source(it.sourceID)) }
        }
        repo.routes.forEach { route ->
            assertTrue("${route.id} should have citations", route.citations.isNotEmpty())
            route.citations.forEach { assertNotNull(repo.source(it.sourceID)) }
        }
        repo.audioStops.forEach { stop ->
            assertTrue("${stop.id} should have citations", stop.citations.isNotEmpty())
            stop.citations.forEach { assertNotNull(repo.source(it.sourceID)) }
        }
        repo.sourceChunks.forEach { chunk ->
            assertNotNull(repo.source(chunk.sourceID))
            assertNotNull(repo.source(chunk.citation.sourceID))
        }
    }

    @Test
    fun routesResolveAudioStopsAndLinkedContent() {
        repo.routes.forEach { route ->
            route.stopIDs.forEach { stopID ->
                assertNotNull("Missing stop $stopID for ${route.id}", repo.audioStop(stopID))
            }
        }
        repo.audioStops.forEach { stop ->
            val resolved = when (stop.linkedKind.name) {
                "WORK" -> repo.work(stop.linkedID)
                "TOPIC" -> repo.topic(stop.linkedID)
                "ROUTE" -> repo.route(stop.linkedID)
                "SOURCE" -> repo.source(stop.linkedID)
                else -> stop
            }
            assertNotNull("Missing linked content for ${stop.id}", resolved)
        }
    }
}

