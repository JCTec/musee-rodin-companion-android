package com.museerodin.companion

import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.ContentFileDataSource
import com.museerodin.companion.search.SearchResultKind
import com.museerodin.companion.search.SearchService
import com.museerodin.companion.user.ReadingNote
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchServiceTest {
    private val repo = ContentFileDataSource.load(File("src/main/assets/content"))

    @Test
    fun searchFindsWorksTopicsRoutesSourceChunksAndNotesDeterministically() {
        val notes = listOf(
            ReadingNote(
                id = "note-test",
                linkedKindRaw = "work",
                linkedID = "work-balzac",
                title = "Balzac visit thought",
                body = "Compare visitor route pacing with the garden.",
                createdAt = 1,
                updatedAt = 1,
            ),
        )

        val balzac = SearchService.search("Balzac", repo, notes, AppLanguage.EN)
        assertTrue(balzac.any { it.kind == SearchResultKind.WORK && it.linkedID == "work-balzac" })
        assertTrue(balzac.any { it.kind == SearchResultKind.NOTE && it.id == "note-note-test" })

        val visitor = SearchService.search("visitor", repo, notes, AppLanguage.EN)
        assertTrue(visitor.any { it.kind == SearchResultKind.ROUTE || it.kind == SearchResultKind.TOPIC })
        assertTrue(visitor.any { it.kind == SearchResultKind.SOURCE_CHUNK })

        val repeat = SearchService.search("visitor", repo, notes, AppLanguage.EN)
        assertEquals(visitor.map { it.id }, repeat.map { it.id })
    }

    @Test
    fun blankSearchReturnsNoResults() {
        assertEquals(emptyList<String>(), SearchService.search("   ", repo, emptyList(), AppLanguage.EN).map { it.id })
    }
}

