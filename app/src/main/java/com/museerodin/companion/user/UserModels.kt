package com.museerodin.companion.user

enum class UserLinkedKind(val rawValue: String) {
    WORK("work"),
    TOPIC("topic"),
    ROUTE("route"),
    SOURCE("source"),
    AUDIO_STOP("audioStop"),
}

enum class PlaybackState(val rawValue: String) {
    IDLE("idle"),
    SPEAKING("speaking"),
    PAUSED("paused"),
    STOPPED("stopped"),
    COMPLETED("completed");

    companion object {
        fun fromRaw(rawValue: String?): PlaybackState {
            return entries.firstOrNull { it.rawValue == rawValue } ?: IDLE
        }
    }
}

data class ReadingNote(
    val id: String,
    val linkedKindRaw: String,
    val linkedID: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class FavoriteRecord(
    val id: String,
    val linkedKindRaw: String,
    val linkedID: String,
    val createdAt: Long,
)

data class SeenRecord(
    val id: String,
    val workID: String,
    val createdAt: Long,
)

data class RouteProgressRecord(
    val routeID: String,
    val currentStopID: String?,
    val completedStopIDs: List<String>,
    val updatedAt: Long,
)

data class PlaybackProgressRecord(
    val id: String,
    val routeID: String?,
    val stopID: String?,
    val speed: Double,
    val state: PlaybackState,
    val updatedAt: Long,
)

