package com.museerodin.companion.user

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "reading_notes")
data class ReadingNoteEntity(
    @PrimaryKey val id: String,
    val linkedKindRaw: String,
    val linkedID: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "favorite_records")
data class FavoriteRecordEntity(
    @PrimaryKey val id: String,
    val linkedKindRaw: String,
    val linkedID: String,
    val createdAt: Long,
)

@Entity(tableName = "seen_records")
data class SeenRecordEntity(
    @PrimaryKey val id: String,
    val workID: String,
    val createdAt: Long,
)

@Entity(tableName = "route_progress")
data class RouteProgressEntity(
    @PrimaryKey val routeID: String,
    val currentStopID: String?,
    val completedStopIDs: List<String>,
    val updatedAt: Long,
)

@Entity(tableName = "playback_progress")
data class PlaybackProgressEntity(
    @PrimaryKey val id: String = "global-playback",
    val routeID: String?,
    val stopID: String?,
    val speed: Double,
    @ColumnInfo(name = "stateRaw") val stateRaw: String,
    val updatedAt: Long,
)

class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = value.joinToString(separator = "\u001F")

    @TypeConverter
    fun toList(value: String): List<String> = if (value.isBlank()) emptyList() else value.split("\u001F")
}

@Dao
interface UserDao {
    @Query("SELECT * FROM reading_notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<ReadingNoteEntity>>

    @Query("SELECT * FROM favorite_records ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<FavoriteRecordEntity>>

    @Query("SELECT * FROM seen_records ORDER BY createdAt DESC")
    fun observeSeen(): Flow<List<SeenRecordEntity>>

    @Query("SELECT * FROM route_progress WHERE routeID = :routeID")
    fun observeRouteProgress(routeID: String): Flow<RouteProgressEntity?>

    @Query("SELECT * FROM playback_progress WHERE id = 'global-playback'")
    fun observePlaybackProgress(): Flow<PlaybackProgressEntity?>

    @Query("SELECT * FROM reading_notes WHERE id = :id")
    suspend fun note(id: String): ReadingNoteEntity?

    @Query("SELECT * FROM favorite_records WHERE id = :id")
    suspend fun favorite(id: String): FavoriteRecordEntity?

    @Query("SELECT * FROM seen_records WHERE id = :workID")
    suspend fun seen(workID: String): SeenRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: ReadingNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(record: FavoriteRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSeen(record: SeenRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRouteProgress(record: RouteProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaybackProgress(record: PlaybackProgressEntity)

    @Query("DELETE FROM reading_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("DELETE FROM favorite_records WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("DELETE FROM seen_records WHERE id = :workID")
    suspend fun deleteSeen(workID: String)

    @Query("DELETE FROM reading_notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM favorite_records")
    suspend fun deleteAllFavorites()

    @Query("DELETE FROM seen_records")
    suspend fun deleteAllSeen()
}

@Database(
    entities = [
        ReadingNoteEntity::class,
        FavoriteRecordEntity::class,
        SeenRecordEntity::class,
        RouteProgressEntity::class,
        PlaybackProgressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val NAME = "musee_rodin_user_state.db"

        fun create(context: Context): UserDatabase {
            return Room.databaseBuilder(context, UserDatabase::class.java, NAME).build()
        }
    }
}

fun ReadingNoteEntity.toDomain(): ReadingNote = ReadingNote(id, linkedKindRaw, linkedID, title, body, createdAt, updatedAt)
fun FavoriteRecordEntity.toDomain(): FavoriteRecord = FavoriteRecord(id, linkedKindRaw, linkedID, createdAt)
fun SeenRecordEntity.toDomain(): SeenRecord = SeenRecord(id, workID, createdAt)
fun RouteProgressEntity.toDomain(): RouteProgressRecord = RouteProgressRecord(routeID, currentStopID, completedStopIDs, updatedAt)
fun PlaybackProgressEntity.toDomain(): PlaybackProgressRecord = PlaybackProgressRecord(id, routeID, stopID, speed, PlaybackState.fromRaw(stateRaw), updatedAt)

