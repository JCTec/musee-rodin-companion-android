package com.museerodin.companion.robots

import com.museerodin.companion.ui.A11yTags

class WorkRowRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(id: String): WorkRowRobot {
        assertTagVisible("work.row.$id")
        return this
    }
}

class WorkArtworkRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(id: String): WorkArtworkRobot {
        assertTagVisible("work.image.$id")
        return this
    }

    fun openFullScreen(id: String): WorkArtworkRobot {
        assertContentDescription("Open full screen artwork image")
        tapTag("work.image.$id")
        assertTagVisible("work.image.fullscreen.$id")
        return this
    }

    fun closeFullScreen(id: String): WorkArtworkRobot {
        tapTag("work.image.fullscreen.close.$id")
        return this
    }
}

class CitationChipRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(id: String): CitationChipRobot {
        assertTagVisible("citation.$id")
        return this
    }
}

class ReadAloudButtonRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): ReadAloudButtonRobot {
        assertTagVisible(A11yTags.ReadAloudButton)
        return this
    }

    fun toggleTwice(): ReadAloudButtonRobot {
        tapFirstTag(A11yTags.ReadAloudButton)
        tapFirstTag(A11yTags.ReadAloudButton)
        return this
    }
}

class PlaceholderPanelRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(label: String): PlaceholderPanelRobot {
        assertContentDescription(label)
        return this
    }
}

class ConfidenceChipRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(label: String): ConfidenceChipRobot {
        assertTagVisible("confidence.$label")
        return this
    }
}

class MetadataGridRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): MetadataGridRobot {
        assertTagVisible("metadata.grid")
        return this
    }
}

class TagChipRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(title: String): TagChipRobot {
        assertTagVisible("tag.$title")
        return this
    }
}
