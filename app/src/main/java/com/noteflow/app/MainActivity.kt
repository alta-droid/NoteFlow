package com.noteflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noteflow.app.ui.screens.CreateEditScreen
import com.noteflow.app.ui.screens.HomeScreen
import com.noteflow.app.ui.screens.NoteDetailScreen
import com.noteflow.app.ui.theme.NoteFlowTheme
import com.noteflow.app.viewmodel.NoteViewModel

import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteFlowTheme {
                NoteFlowNavigation()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NoteFlowNavigation() {
    val navController = rememberNavController()
    val viewModel: NoteViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            fadeIn(tween(280)) + slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(280))
        },
        exitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(tween(280)) + slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280))
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(200))
        }
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNoteClick = { id -> navController.navigate("detail/$id") },
                onAddNote = { navController.navigate("create") },
                onChatClick = { navController.navigate("chat") }
            )
        }

        composable("create") {
            CreateEditScreen(
                viewModel = viewModel,
                noteId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
            CreateEditScreen(
                viewModel = viewModel,
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
            NoteDetailScreen(
                viewModel = viewModel,
                noteId = noteId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("edit/$id") }
            )
        }

        composable("chat") {
            val chatViewModel: com.noteflow.app.viewmodel.ChatViewModel = hiltViewModel()
            com.noteflow.app.ui.screens.ChatScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
