package com.museerodin.companion

import android.app.UiModeManager
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.museerodin.companion.robots.CitationChipRobot
import com.museerodin.companion.robots.ConfidenceChipRobot
import com.museerodin.companion.robots.MetadataGridRobot
import com.museerodin.companion.robots.NotesRobot
import com.museerodin.companion.robots.PlaceholderPanelRobot
import com.museerodin.companion.robots.PlacesRobot
import com.museerodin.companion.robots.ReadAloudButtonRobot
import com.museerodin.companion.robots.RootRobot
import com.museerodin.companion.robots.TagChipRobot
import com.museerodin.companion.robots.WorkRowRobot
import com.museerodin.companion.robots.WorkArtworkRobot
import com.museerodin.companion.ui.A11yTags
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuseeRodinCompanionUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetUserState() {
        val app = rule.activity.application as MuseeRodinApplication
        runBlocking { app.userRepository.clearForTests() }
    }

    @Test
    fun robotDrivenCoreWorkNoteFavoriteSeenFlow() {
        val works = PlacesRobot(rule)
            .assertVisible()
            .goToWorks()
            .filter("Rodin")
            .filter("All")
            .search("Thinker")

        WorkRowRobot(rule).assertVisible("work-le-penseur")

        val workDetail = works
            .openFirstWork()
            .readAloudPauseResumeStop()
            .favoriteAndMarkSeen()

        WorkArtworkRobot(rule).assertVisible("work-le-penseur")
        ReadAloudButtonRobot(rule).assertVisible()
        ConfidenceChipRobot(rule).assertVisible("Verified")
        MetadataGridRobot(rule).assertVisible()
        TagChipRobot(rule).assertVisible("rodin")

        workDetail
            .addNote()
            .enter("Robot note", "This note was created by the Android UI robot.")
            .saveToWorkDetail()

        RootRobot(rule).tapTab("Notes")
        val notesRobot = NotesRobot(rule).assertVisible()
        notesRobot.showFavorites().assertText("The Thinker")
        notesRobot.showSeen().assertText("The Thinker")
    }

    @Test
    fun robotDrivenPathsSearchAndLinkedDetailFlow() {
        val pathDetail = PlacesRobot(rule)
            .assertVisible()
            .goToPaths()
            .openHighlights()
            .controlSpeech()
            .markStopDone("stop-le-penseur")

        pathDetail.openLinkedItem("stop-le-penseur")
            .assertVisible()

        RootRobot(rule).tapTab("Search")
        com.museerodin.companion.robots.SearchRobot(rule)
            .assertVisible()
            .search("Balzac")
            .assertText("Monument to Balzac")

        com.museerodin.companion.robots.SearchRobot(rule)
            .search("visitor")
            .assertText("Visit Context")
    }

    @Test
    fun robotCoverageForPlacesReusableComponentsAndCitationInspection() {
        RootRobot(rule).assertVisible()

        PlacesRobot(rule)
            .assertVisible()
            .openHotelBironPlace()
            .assertVisible()

        PlaceholderPanelRobot(rule).assertVisible("topic placeholder")
        RootRobot(rule).tapTag("nav.back")

        PlacesRobot(rule)
            .assertVisible()
            .openGardenPlace()
            .assertVisible()
        RootRobot(rule).tapTag("nav.back")

        PlacesRobot(rule)
            .assertVisible()
            .goToWorks()
            .assertVisible()

        WorkRowRobot(rule).assertVisible("work-le-penseur")
        PlacesRobot(rule).tapTag("work.row.work-le-penseur")

        com.museerodin.companion.robots.WorkDetailRobot(rule)
            .assertVisible()
            .revealSources()

        CitationChipRobot(rule).assertVisible("cite-work-le-penseur")
        rule.onNodeWithContentDescription("Open Official page", substring = true).assertIsDisplayed()
    }

    @Test
    fun notesRobotAddEditDeleteFlowAndAccessibilityDisplaySmoke() {
        RootRobot(rule).tapTab("Notes")
        val notesRobot = NotesRobot(rule)
            .assertVisible()
            .openAddNote()
            .enter("Standalone robot note", "A note started from the Notes tab.")
            .save()
            .assertVisible()
        notesRobot.assertText("Standalone robot note")
        notesRobot.editFirstNote()
            .enter("Edited robot note", "Edited body from robot.")
            .save()
            .assertVisible()
        notesRobot.assertText("Edited robot note")
        notesRobot.deleteFirstNote().assertVisible()

        try {
            setFontScale("1.3")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = rule.activity.getSystemService(UiModeManager::class.java)
                manager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                rule.activity.recreate()
            }
            rule.waitForIdle()

            RootRobot(rule).tapTab("Places")
            PlacesRobot(rule).assertVisible()
            RootRobot(rule).tapTab("Paths")
            com.museerodin.companion.robots.PathsRobot(rule).assertVisible()
            RootRobot(rule).tapTab("Search")
            rule.onNodeWithTag(A11yTags.SearchField).assertIsDisplayed()
        } finally {
            setFontScale("1.0")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = rule.activity.getSystemService(UiModeManager::class.java)
                manager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
            }
        }
    }

    private fun setFontScale(value: String) {
        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand("settings put system font_scale $value")
            .close()
    }
}
