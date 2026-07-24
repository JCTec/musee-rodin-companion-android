package com.museerodin.companion.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.museerodin.companion.MainActivity

typealias RodinComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

open class BaseRobot(protected val rule: RodinComposeRule) {
    fun assertTagVisible(tag: String): BaseRobot {
        if (rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()) {
            scrollFirstContainerToTag(tag)
        }
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val node = rule.onAllNodesWithTag(tag, useUnmergedTree = true).onFirst()
        try {
            node.performScrollTo()
        } catch (_: Throwable) {
            // The node is already visible or not inside a scroll container.
        }
        node.assertIsDisplayed()
        return this
    }

    fun assertText(text: String): BaseRobot {
        if (rule.onAllNodesWithText(text, substring = true, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()) {
            scrollFirstContainerToText(text)
        }
        rule.waitUntil(5_000) {
            rule.onAllNodesWithText(text, substring = true, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val node = rule.onAllNodesWithText(text, substring = true, useUnmergedTree = true).onFirst()
        try {
            node.performScrollTo()
        } catch (_: Throwable) {
            // The node is already visible or not inside a scroll container.
        }
        node.assertIsDisplayed()
        return this
    }

    fun assertContentDescription(text: String): BaseRobot {
        rule.waitUntil(5_000) {
            rule.onAllNodesWithContentDescription(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onAllNodesWithContentDescription(text, substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        return this
    }

    fun tapTag(tag: String): BaseRobot {
        val node = rule.onNodeWithTag(tag, useUnmergedTree = true)
        try {
            node.performScrollTo()
        } catch (_: Throwable) {
            // The node is already in a non-scrollable surface.
        }
        node.performClick()
        rule.waitForIdle()
        return this
    }

    fun tapFirstTag(tag: String): BaseRobot {
        rule.onAllNodesWithTag(tag).onFirst().performClick()
        rule.waitForIdle()
        return this
    }

    fun tapText(text: String): BaseRobot {
        val node = rule.onNodeWithText(text, substring = true, useUnmergedTree = true)
        try {
            node.performScrollTo()
        } catch (_: Throwable) {
            // The node is already in a non-scrollable surface.
        }
        node.performClick()
        rule.waitForIdle()
        return this
    }

    fun tapTab(label: String): BaseRobot {
        val tag = "tab.${label.lowercase()}"
        val sidebar = "sidebar.${label.lowercase()}"
        val tabExists = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        if (tabExists) {
            rule.onNodeWithTag(tag).performClick()
        } else {
            rule.onNodeWithTag(sidebar).performClick()
        }
        rule.waitForIdle()
        return this
    }

    private fun scrollFirstContainerToTag(tag: String) {
        try {
            rule.onAllNodes(hasScrollAction(), useUnmergedTree = true)
                .onFirst()
                .performScrollToNode(hasTestTag(tag))
            rule.waitForIdle()
        } catch (_: Throwable) {
            // No scrollable container currently exposes the requested tag.
        }
    }

    private fun scrollFirstContainerToText(text: String) {
        try {
            rule.onAllNodes(hasScrollAction(), useUnmergedTree = true)
                .onFirst()
                .performScrollToNode(hasText(text, substring = true))
            rule.waitForIdle()
        } catch (_: Throwable) {
            // No scrollable container currently exposes the requested text.
        }
    }

}

class RootRobot(rule: RodinComposeRule) : BaseRobot(rule) {
    fun assertVisible(): RootRobot {
        val found = rule.onAllNodesWithTag("tab.places").fetchSemanticsNodes().isNotEmpty() ||
            rule.onAllNodesWithTag("sidebar.places").fetchSemanticsNodes().isNotEmpty()
        check(found) { "Root navigation was not visible." }
        return this
    }
}
