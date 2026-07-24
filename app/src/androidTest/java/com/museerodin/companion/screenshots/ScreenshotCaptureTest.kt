package com.museerodin.companion.screenshots

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.museerodin.companion.MainActivity
import com.museerodin.companion.MuseeRodinApplication
import com.museerodin.companion.robots.NotesRobot
import com.museerodin.companion.robots.PathsRobot
import com.museerodin.companion.robots.PlacesRobot
import com.museerodin.companion.robots.RootRobot
import com.museerodin.companion.robots.SearchRobot
import com.museerodin.companion.robots.WorkDetailRobot
import com.museerodin.companion.robots.WorksRobot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ScreenshotCaptureTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetUserState() {
        val app = rule.activity.application as MuseeRodinApplication
        runBlocking { app.userRepository.clearForTests() }
    }

    @Test
    fun captureScreenshotMatrix() {
        PlacesRobot(rule).assertVisible()
        capture("places", "list", "system")

        PlacesRobot(rule).openHotelBironPlace()
        capture("places", "hotel_biron_detail", "system")
        RootRobot(rule).tapTag("nav.back")

        RootRobot(rule).tapTab("Works")
        WorksRobot(rule).assertVisible()
        capture("works", "all_filter", "system")
        WorksRobot(rule).filter("Rodin")
        capture("works", "rodin_filter", "system")
        WorksRobot(rule).filter("Claudel")
        capture("works", "claudel_filter", "system")
        WorksRobot(rule).filter("All").search("Thinker")
        capture("works", "search_thinker", "system")

        WorksRobot(rule).openFirstWork()
        WorkDetailRobot(rule).assertVisible()
        capture("works", "work_detail_default", "system")
        WorkDetailRobot(rule).revealSources()
        capture("works", "work_detail_sources_expanded", "system")
        WorkDetailRobot(rule).favoriteAndMarkSeen()
        capture("works", "work_detail_favorited_seen", "system")

        RootRobot(rule).tapTab("Notes")
        NotesRobot(rule).assertVisible().showFavorites()
        capture("notes", "favorites", "system")
        NotesRobot(rule).openAddNote()
        capture("notes", "editor", "system")
        RootRobot(rule).tapTag("nav.back")

        RootRobot(rule).tapTab("Paths")
        PathsRobot(rule).assertVisible()
        capture("paths", "list", "system")
        PathsRobot(rule).openHighlights()
        capture("paths", "highlights_detail_idle", "system")

        RootRobot(rule).tapTab("Search")
        SearchRobot(rule).assertVisible()
        capture("search", "empty", "system")
        SearchRobot(rule).search("Balzac")
        capture("search", "work_results", "system")
        SearchRobot(rule).search("zzzzzzzz")
        capture("search", "no_results", "system")
    }

    private fun capture(view: String, state: String, appearance: String) {
        rule.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.getExternalFilesDir(null), "screenshots")
        dir.mkdirs()
        val file = File(dir, "android__${view}__${state}__${appearance}.png")
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        file.outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
}
