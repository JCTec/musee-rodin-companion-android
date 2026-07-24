package com.museerodin.companion.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepository(
    private val dao: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    val notes: Flow<List<ReadingNote>> = dao.observeNotes().map { records -> records.map(ReadingNoteEntity::toDomain) }
    val favorites: Flow<List<FavoriteRecord>> = dao.observeFavorites().map { records -> records.map(FavoriteRecordEntity::toDomain) }
    val seen: Flow<List<SeenRecord>> = dao.observeSeen().map { records -> records.map(SeenRecordEntity::toDomain) }
    val playbackProgress: Flow<PlaybackProgressRecord?> = dao.observePlaybackProgress().map { it?.toDomain() }

    fun routeProgress(routeID: String): Flow<RouteProgressRecord?> {
        return dao.observeRouteProgress(routeID).map { it?.toDomain() }
    }

    suspend fun createNote(
        linkedKind: UserLinkedKind,
        linkedID: String,
        title: String,
        body: String,
    ): String = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        dao.upsertNote(
            ReadingNoteEntity(
                id = id,
                linkedKindRaw = linkedKind.rawValue,
                linkedID = linkedID,
                title = title.trim(),
                body = body.trim(),
                createdAt = now,
                updatedAt = now,
            ),
        )
        id
    }

    suspend fun updateNote(id: String, title: String, body: String) = withContext(ioDispatcher) {
        val existing = dao.note(id) ?: return@withContext
        dao.upsertNote(existing.copy(title = title.trim(), body = body.trim(), updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(id: String) = withContext(ioDispatcher) {
        dao.deleteNote(id)
    }

    suspend fun toggleFavorite(kind: UserLinkedKind, linkedID: String): Boolean = withContext(ioDispatcher) {
        val id = "${kind.rawValue}-$linkedID"
        val existing = dao.favorite(id)
        if (existing == null) {
            dao.upsertFavorite(FavoriteRecordEntity(id, kind.rawValue, linkedID, System.currentTimeMillis()))
            true
        } else {
            dao.deleteFavorite(id)
            false
        }
    }

    suspend fun toggleSeen(workID: String): Boolean = withContext(ioDispatcher) {
        val existing = dao.seen(workID)
        if (existing == null) {
            dao.upsertSeen(SeenRecordEntity(id = workID, workID = workID, createdAt = System.currentTimeMillis()))
            true
        } else {
            dao.deleteSeen(workID)
            false
        }
    }

    suspend fun setRouteProgress(routeID: String, currentStopID: String?, completedStopIDs: List<String>) = withContext(ioDispatcher) {
        dao.upsertRouteProgress(
            RouteProgressEntity(
                routeID = routeID,
                currentStopID = currentStopID,
                completedStopIDs = completedStopIDs.distinct(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setPlaybackProgress(
        routeID: String?,
        stopID: String?,
        speed: Double,
        state: PlaybackState,
    ) = withContext(ioDispatcher) {
        dao.upsertPlaybackProgress(
            PlaybackProgressEntity(
                routeID = routeID,
                stopID = stopID,
                speed = speed,
                stateRaw = state.rawValue,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearForTests() = withContext(ioDispatcher) {
        dao.deleteAllNotes()
        dao.deleteAllFavorites()
        dao.deleteAllSeen()
    }
}
