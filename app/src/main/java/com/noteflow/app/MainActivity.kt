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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: com.noteflow.app.viewmodel.SettingsViewModel = hiltViewModel()
            val appTheme by settingsViewModel.appTheme.collectAsState()
            
            val darkTheme = when (appTheme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            NoteFlowTheme(darkTheme = darkTheme) {
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
                onSettingsClick = { navController.navigate("settings") },
                onAdvisorClick = { navController.navigate("advisor") }
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

        composable("settings") {
            val settingsViewModel: com.noteflow.app.viewmodel.SettingsViewModel = hiltViewModel()
            com.noteflow.app.ui.screens.SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("advisor") {
            val advisorViewModel: com.noteflow.app.viewmodel.AdvisorViewModel = hiltViewModel()
            com.noteflow.app.ui.screens.AdvisorScreen(
                viewModel = advisorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
