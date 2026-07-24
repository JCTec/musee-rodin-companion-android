package com.museerodin.companion.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.ui.screens.NoteEditorScreen
import com.museerodin.companion.ui.screens.NotesScreen
import com.museerodin.companion.ui.screens.PathDetailScreen
import com.museerodin.companion.ui.screens.PathsScreen
import com.museerodin.companion.ui.screens.PlacesScreen
import com.museerodin.companion.ui.screens.SearchScreen
import com.museerodin.companion.ui.screens.TopicDetailScreen
import com.museerodin.companion.ui.screens.WorkDetailScreen
import com.museerodin.companion.ui.screens.WorksScreen
import com.museerodin.companion.user.UserLinkedKind
import com.museerodin.companion.user.UserRepository

private sealed class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val tag: String,
) {
    data object Places : TopDestination("places", "Places", Icons.Filled.Place, "tab.places")
    data object Works : TopDestination("works", "Works", Icons.Filled.Star, "tab.works")
    data object Paths : TopDestination("paths", "Paths", Icons.Filled.Map, "tab.paths")
    data object Search : TopDestination("search", "Search", Icons.Filled.Search, "tab.search")
    data object Notes : TopDestination("notes", "Notes", Icons.Filled.Notes, "tab.notes")
}

private val TopDestinations = listOf(
    TopDestination.Places,
    TopDestination.Works,
    TopDestination.Paths,
    TopDestination.Search,
    TopDestination.Notes,
)

object RodinRoutes {
    const val WorkDetailPattern = "work/{workId}"
    const val TopicDetailPattern = "topic/{topicId}"
    const val PathDetailPattern = "path/{routeId}"
    const val NoteEditorPattern = "note-editor?noteId={noteId}&linkedKind={linkedKind}&linkedID={linkedID}&suggestedTitle={suggestedTitle}"

    fun workDetail(workId: String): String = "work/$workId"
    fun topicDetail(topicId: String): String = "topic/$topicId"
    fun pathDetail(routeId: String): String = "path/$routeId"
    fun noteEditor(
        noteId: String? = null,
        linkedKind: UserLinkedKind = UserLinkedKind.TOPIC,
        linkedID: String = "topic-archives",
        suggestedTitle: String = "Research note",
    ): String {
        return "note-editor?noteId=${noteId.orEmpty().encoded()}&linkedKind=${linkedKind.rawValue.encoded()}&linkedID=${linkedID.encoded()}&suggestedTitle=${suggestedTitle.encoded()}"
    }

    fun forContent(kind: ContentLinkKind, id: String): String? = when (kind) {
        ContentLinkKind.WORK -> workDetail(id)
        ContentLinkKind.TOPIC -> topicDetail(id)
        ContentLinkKind.ROUTE -> pathDetail(id)
        ContentLinkKind.SOURCE,
        ContentLinkKind.AUDIO_STOP,
        -> null
    }
}

@Composable
fun RodinApp(
    contentRepository: ContentRepository,
    userRepository: UserRepository,
    narrationController: NarrationController,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val expanded = maxWidth >= 700.dp
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                RodinNavigationRail(navController)
                RodinNavScaffold(
                    navController = navController,
                    showBottomBar = false,
                    contentRepository = contentRepository,
                    userRepository = userRepository,
                    narrationController = narrationController,
                )
            }
        } else {
            RodinNavScaffold(
                navController = navController,
                showBottomBar = true,
                contentRepository = contentRepository,
                userRepository = userRepository,
                narrationController = narrationController,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RodinNavScaffold(
    navController: NavHostController,
    showBottomBar: Boolean,
    contentRepository: ContentRepository,
    userRepository: UserRepository,
    narrationController: NarrationController,
) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val canNavigateBack = route !in TopDestinations.map { it.route }
    val title = TopDestinations.firstOrNull { route == it.route }?.label ?: "Musee Rodin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.testTag("nav.back"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (showBottomBar) {
                RodinBottomNavigation(navController)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            RodinNavHost(
                navController = navController,
                contentRepository = contentRepository,
                userRepository = userRepository,
                narrationController = narrationController,
            )
        }
    }
}

@Composable
private fun RodinNavHost(
    navController: NavHostController,
    contentRepository: ContentRepository,
    userRepository: UserRepository,
    narrationController: NarrationController,
) {
    val notes by userRepository.notes.collectAsStateWithLifecycle(emptyList())
    val favorites by userRepository.favorites.collectAsStateWithLifecycle(emptyList())
    val seen by userRepository.seen.collectAsStateWithLifecycle(emptyList())

    NavHost(navController = navController, startDestination = TopDestination.Places.route) {
        composable(TopDestination.Places.route) {
            PlacesScreen(contentRepository, onOpenTopic = { navController.navigate(RodinRoutes.topicDetail(it)) })
        }
        composable(TopDestination.Works.route) {
            WorksScreen(contentRepository, onOpenWork = { navController.navigate(RodinRoutes.workDetail(it)) })
        }
        composable(TopDestination.Paths.route) {
            PathsScreen(contentRepository, onOpenPath = { navController.navigate(RodinRoutes.pathDetail(it)) })
        }
        composable(TopDestination.Search.route) {
            SearchScreen(
                contentRepository = contentRepository,
                notes = notes,
                onOpenResult = { kind, id ->
                    RodinRoutes.forContent(kind, id)?.let { navController.navigate(it) }
                },
            )
        }
        composable(TopDestination.Notes.route) {
            NotesScreen(
                contentRepository = contentRepository,
                notes = notes,
                favorites = favorites,
                seen = seen,
                userRepository = userRepository,
                onOpenWork = { navController.navigate(RodinRoutes.workDetail(it)) },
                onAddNote = { navController.navigate(RodinRoutes.noteEditor()) },
                onEditNote = { navController.navigate(RodinRoutes.noteEditor(noteId = it)) },
            )
        }
        composable(
            route = RodinRoutes.WorkDetailPattern,
            arguments = listOf(navArgument("workId") { type = NavType.StringType }),
        ) { entry ->
            val workID = entry.arguments?.getString("workId").orEmpty()
            val work = contentRepository.work(workID)
            if (work != null) {
                WorkDetailScreen(
                    work = work,
                    contentRepository = contentRepository,
                    favorites = favorites,
                    seen = seen,
                    userRepository = userRepository,
                    narrationController = narrationController,
                    onAddNote = {
                        navController.navigate(
                            RodinRoutes.noteEditor(
                                linkedKind = UserLinkedKind.WORK,
                                linkedID = work.id,
                                suggestedTitle = work.title.value(com.museerodin.companion.content.AppLanguage.fromLocale()),
                            ),
                        )
                    },
                )
            }
        }
        composable(
            route = RodinRoutes.TopicDetailPattern,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { entry ->
            val topicID = entry.arguments?.getString("topicId").orEmpty()
            val topic = contentRepository.topic(topicID)
            if (topic != null) {
                TopicDetailScreen(
                    topic = topic,
                    contentRepository = contentRepository,
                    onOpenWork = { navController.navigate(RodinRoutes.workDetail(it)) },
                )
            }
        }
        composable(
            route = RodinRoutes.PathDetailPattern,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType }),
        ) { entry ->
            val routeID = entry.arguments?.getString("routeId").orEmpty()
            val route = contentRepository.route(routeID)
            if (route != null) {
                PathDetailScreen(
                    route = route,
                    contentRepository = contentRepository,
                    userRepository = userRepository,
                    narrationController = narrationController,
                    onOpenLinkedItem = { kind, id ->
                        RodinRoutes.forContent(kind, id)?.let { navController.navigate(it) }
                    },
                )
            }
        }
        composable(
            route = RodinRoutes.NoteEditorPattern,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("linkedKind") {
                    type = NavType.StringType
                    defaultValue = UserLinkedKind.TOPIC.rawValue
                },
                navArgument("linkedID") {
                    type = NavType.StringType
                    defaultValue = "topic-archives"
                },
                navArgument("suggestedTitle") {
                    type = NavType.StringType
                    defaultValue = "Research note"
                },
            ),
        ) { entry ->
            val noteID = entry.arguments?.getString("noteId")?.takeIf { it.isNotBlank() }
            val rawKind = entry.arguments?.getString("linkedKind") ?: UserLinkedKind.TOPIC.rawValue
            val linkedKind = UserLinkedKind.entries.firstOrNull { it.rawValue == rawKind } ?: UserLinkedKind.TOPIC
            NoteEditorScreen(
                noteID = noteID,
                linkedKind = linkedKind,
                linkedID = entry.arguments?.getString("linkedID") ?: "topic-archives",
                suggestedTitle = entry.arguments?.getString("suggestedTitle") ?: "Research note",
                notes = notes,
                userRepository = userRepository,
                onClose = {
                    val linkedID = entry.arguments?.getString("linkedID") ?: "topic-archives"
                    if (linkedKind == UserLinkedKind.TOPIC && linkedID == "topic-archives") {
                        navController.navigate(TopDestination.Notes.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
            )
        }
    }
}

@Composable
private fun RodinBottomNavigation(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    NavigationBar {
        TopDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateTop(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) },
                modifier = Modifier.testTag(destination.tag),
            )
        }
    }
}

@Composable
private fun RodinNavigationRail(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    NavigationRail {
        TopDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = { navController.navigateTop(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) },
                modifier = Modifier.testTag("sidebar.${destination.route}"),
            )
        }
    }
}

private fun NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun String.encoded(): String = Uri.encode(this)
