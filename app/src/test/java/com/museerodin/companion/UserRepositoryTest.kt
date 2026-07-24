package com.museerodin.companion

import com.museerodin.companion.user.FavoriteRecordEntity
import com.museerodin.companion.user.PlaybackProgressEntity
import com.museerodin.companion.user.PlaybackState
import com.museerodin.companion.user.ReadingNoteEntity
import com.museerodin.companion.user.RouteProgressEntity
import com.museerodin.companion.user.SeenRecordEntity
import com.museerodin.companion.user.UserDao
import com.museerodin.companion.user.UserLinkedKind
import com.museerodin.companion.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private val dao = FakeUserDao()
    private val repository = UserRepository(dao, dispatcher)

    @Test
    fun createsUpdatesAndDeletesNotes() = runTest(dispatcher) {
        val id = repository.createNote(UserLinkedKind.WORK, "work-le-penseur", " First note ", " Body ")
        assertEquals("First note", dao.notes.value.single().title)
        assertEquals("Body", dao.notes.value.single().body)

        repository.updateNote(id, "Updated", "Updated body")
        assertEquals("Updated", dao.notes.value.single().title)

        repository.deleteNote(id)
        assertTrue(dao.notes.value.isEmpty())
    }

    @Test
    fun togglesFavoriteSeenRouteAndPlaybackState() = runTest(dispatcher) {
        assertTrue(repository.toggleFavorite(UserLinkedKind.WORK, "work-le-penseur"))
        assertEquals("work-work-le-penseur", dao.favorites.value.single().id)
        assertFalse(repository.toggleFavorite(UserLinkedKind.WORK, "work-le-penseur"))
        assertTrue(dao.favorites.value.isEmpty())

        assertTrue(repository.toggleSeen("work-le-penseur"))
        assertEquals("work-le-penseur", dao.seen.value.single().workID)
        assertFalse(repository.toggleSeen("work-le-penseur"))
        assertTrue(dao.seen.value.isEmpty())

        repository.setRouteProgress("route-highlights", "stop-le-penseur", listOf("stop-le-penseur"))
        assertEquals(listOf("stop-le-penseur"), dao.routeProgress.value.single().completedStopIDs)

        repository.setPlaybackProgress("route-highlights", "stop-le-penseur", 1.25, PlaybackState.SPEAKING)
        assertEquals("speaking", dao.playback.value?.stateRaw)
    }
}

private class FakeUserDao : UserDao {
    val notes = MutableStateFlow<List<ReadingNoteEntity>>(emptyList())
    val favorites = MutableStateFlow<List<FavoriteRecordEntity>>(emptyList())
    val seen = MutableStateFlow<List<SeenRecordEntity>>(emptyList())
    val routeProgress = MutableStateFlow<List<RouteProgressEntity>>(emptyList())
    val playback = MutableStateFlow<PlaybackProgressEntity?>(null)

    override fun observeNotes(): Flow<List<ReadingNoteEntity>> = notes
    override fun observeFavorites(): Flow<List<FavoriteRecordEntity>> = favorites
    override fun observeSeen(): Flow<List<SeenRecordEntity>> = seen
    override fun observeRouteProgress(routeID: String): Flow<RouteProgressEntity?> = MutableStateFlow(routeProgress.value.firstOrNull { it.routeID == routeID })
    override fun observePlaybackProgress(): Flow<PlaybackProgressEntity?> = playback

    override suspend fun note(id: String): ReadingNoteEntity? = notes.value.firstOrNull { it.id == id }
    override suspend fun favorite(id: String): FavoriteRecordEntity? = favorites.value.firstOrNull { it.id == id }
    override suspend fun seen(workID: String): SeenRecordEntity? = seen.value.firstOrNull { it.id == workID }

    override suspend fun upsertNote(note: ReadingNoteEntity) {
        notes.value = notes.value.filterNot { it.id == note.id } + note
    }

    override suspend fun upsertFavorite(record: FavoriteRecordEntity) {
        favorites.value = favorites.value.filterNot { it.id == record.id } + record
    }

    override suspend fun upsertSeen(record: SeenRecordEntity) {
        seen.value = seen.value.filterNot { it.id == record.id } + record
    }

    override suspend fun upsertRouteProgress(record: RouteProgressEntity) {
        routeProgress.value = routeProgress.value.filterNot { it.routeID == record.routeID } + record
    }

    override suspend fun upsertPlaybackProgress(record: PlaybackProgressEntity) {
        playback.value = record
    }

    override suspend fun deleteNote(id: String) {
        notes.value = notes.value.filterNot { it.id == id }
    }

    override suspend fun deleteFavorite(id: String) {
        favorites.value = favorites.value.filterNot { it.id == id }
    }

    override suspend fun deleteSeen(workID: String) {
        seen.value = seen.value.filterNot { it.id == workID }
    }

    override suspend fun deleteAllNotes() {
        notes.value = emptyList()
    }

    override suspend fun deleteAllFavorites() {
        favorites.value = emptyList()
    }

    override suspend fun deleteAllSeen() {
        seen.value = emptyList()
    }
}

