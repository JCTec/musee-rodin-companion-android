package com.museerodin.companion.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.closeSoftKeyboard
import com.museerodin.companion.ui.A11yTags

class PlacesRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): PlacesRobot {
        assertTagVisible(A11yTags.PlacesView)
        return this
    }

    fun goToWorks(): WorksRobot {
        tapTab("Works")
        return WorksRobot(rule).assertVisible()
    }

    fun goToPaths(): PathsRobot {
        tapTab("Paths")
        return PathsRobot(rule).assertVisible()
    }

    fun openHotelBironPlace(): TopicDetailRobot {
        tapTag("place.row.topic-hotel-biron")
        return TopicDetailRobot(rule).assertVisible()
    }

    fun openGardenPlace(): TopicDetailRobot {
        tapTag("place.row.topic-garden")
        return TopicDetailRobot(rule).assertVisible()
    }

    fun openMeudonPlace(): TopicDetailRobot {
        tapTag("place.row.topic-meudon")
        return TopicDetailRobot(rule).assertVisible()
    }
}

class WorksRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): WorksRobot {
        assertTagVisible(A11yTags.WorksView)
        return this
    }

    fun filter(label: String): WorksRobot {
        tapTag("works.filter.$label")
        return this
    }

    fun search(text: String): WorksRobot {
        rule.onNodeWithTag(A11yTags.WorksSearchField).performTextReplacement(text)
        rule.waitForIdle()
        return this
    }

    fun openFirstWork(): WorkDetailRobot {
        tapTag("work.row.work-le-penseur")
        return WorkDetailRobot(rule).assertVisible()
    }

    fun openBalzac(): WorkDetailRobot {
        tapTag("work.row.work-balzac")
        return WorkDetailRobot(rule).assertVisible()
    }
}

class WorkDetailRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): WorkDetailRobot {
        assertTagVisible(A11yTags.WorkDetailView)
        return this
    }

    fun readAloudPauseResumeStop(): WorkDetailRobot {
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudSpeedButton)
        tapFirstTag(A11yTags.ReadAloudStopButton)
        return this
    }

    fun favoriteAndMarkSeen(): WorkDetailRobot {
        tapTag(A11yTags.FavoriteButton)
        tapTag(A11yTags.SeenButton)
        return this
    }

    fun revealSources(): WorkDetailRobot {
        tapTag("work.sources.toggle")
        return this
    }

    fun addNote(): NoteEditorRobot {
        tapTag(A11yTags.AddNoteButton)
        return NoteEditorRobot(rule).assertVisible()
    }
}

class PathsRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): PathsRobot {
        assertTagVisible(A11yTags.PathsView)
        return this
    }

    fun openHighlights(): PathDetailRobot {
        tapTag("path.row.route-highlights")
        return PathDetailRobot(rule).assertVisible()
    }

    fun openVisitContext(): PathDetailRobot {
        tapTag("path.row.route-visit-context")
        return PathDetailRobot(rule).assertVisible()
    }
}

class PathDetailRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): PathDetailRobot {
        assertTagVisible(A11yTags.PathDetailView)
        return this
    }

    fun controlSpeech(): PathDetailRobot {
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudStopButton)
        return this
    }

    fun markStopDone(stopID: String): PathDetailRobot {
        tapTag("path.stop.done.$stopID")
        return this
    }

    fun openLinkedItem(stopID: String): WorkDetailRobot {
        tapTag("path.stop.itemButton.$stopID")
        return WorkDetailRobot(rule).assertVisible()
    }
}

class TopicDetailRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): TopicDetailRobot {
        assertTagVisible(A11yTags.TopicDetailView)
        return this
    }
}

class SearchRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): SearchRobot {
        assertTagVisible(A11yTags.SearchView)
        return this
    }

    fun search(text: String): SearchRobot {
        rule.onNodeWithTag(A11yTags.SearchField).performTextReplacement(text)
        rule.waitForIdle()
        return this
    }

    fun openResult(resultTag: String): WorkDetailRobot {
        tapTag(resultTag)
        return WorkDetailRobot(rule).assertVisible()
    }
}

class NotesRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): NotesRobot {
        assertTagVisible(A11yTags.NotesView)
        return this
    }

    fun openAddNote(): NoteEditorRobot {
        tapTag(A11yTags.AddNoteButton)
        return NoteEditorRobot(rule).assertVisible()
    }

    fun showFavorites(): NotesRobot {
        tapTag("notes.segment.Favorites")
        return this
    }

    fun showSeen(): NotesRobot {
        tapTag("notes.segment.Seen")
        return this
    }

    fun editFirstNote(): NoteEditorRobot {
        rule.onNodeWithTag(A11yTags.NotesView).assertIsDisplayed()
        tapFirstTag("note.editButton")
        return NoteEditorRobot(rule).assertVisible()
    }

    fun deleteFirstNote(): NotesRobot {
        tapFirstTag(A11yTags.DeleteNoteButton)
        return this
    }
}

class NoteEditorRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): NoteEditorRobot {
        assertTagVisible(A11yTags.NoteEditorView)
        return this
    }

    fun enter(title: String, body: String): NoteEditorRobot {
        rule.onNodeWithTag("note.titleField").performTextReplacement(title)
        rule.onNodeWithTag("note.bodyField").performTextReplacement(body)
        rule.waitForIdle()
        return this
    }

    fun save(): NotesRobot {
        closeSoftKeyboard()
        rule.waitForIdle()
        tapTag(A11yTags.SaveNoteButton)
        return try {
            NotesRobot(rule).assertVisible()
        } catch (_: Throwable) {
            RootRobot(rule).tapTab("Notes")
            NotesRobot(rule).assertVisible()
        }
    }

    fun saveToWorkDetail(): WorkDetailRobot {
        closeSoftKeyboard()
        rule.waitForIdle()
        tapTag(A11yTags.SaveNoteButton)
        return WorkDetailRobot(rule)
    }
}
