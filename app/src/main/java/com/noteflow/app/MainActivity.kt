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

import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController

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
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute == "home" || currentRoute == "pdf_home"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    NavigationBarItem(
                        icon = { Icon(if (currentRoute == "home") Icons.Filled.Description else Icons.Outlined.Description, contentDescription = "الملاحظات") },
                        label = { Text("ملاحظاتي") },
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentRoute == "pdf_home") Icons.Filled.PictureAsPdf else Icons.Outlined.PictureAsPdf, contentDescription = "PDF قارئ") },
                        label = { Text("ملفات PDF") },
                        selected = currentRoute == "pdf_home",
                        onClick = {
                            if (currentRoute != "pdf_home") {
                                navController.navigate("pdf_home") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)) {
            NoteFlowNavigation(navController = navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NoteFlowNavigation(navController: NavHostController) {
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

        composable("pdf_home") {
            val pdfViewModel: com.noteflow.app.viewmodel.PdfViewModel = hiltViewModel()
            com.noteflow.app.ui.screens.PdfHomeScreen(
                viewModel = pdfViewModel,
                onPdfClick = { uriStr ->
                    val encodedUri = java.net.URLEncoder.encode(uriStr, "UTF-8")
                    navController.navigate("pdf_viewer/$encodedUri")
                }
            )
        }
        
        composable(
            route = "pdf_viewer/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: return@composable
            val decodedUri = java.net.URLDecoder.decode(uriStr, "UTF-8")
            val pdfViewModel: com.noteflow.app.viewmodel.PdfViewModel = hiltViewModel()
            com.noteflow.app.ui.screens.PdfViewerScreen(
                viewModel = pdfViewModel,
                uriString = decodedUri,
                onBack = { navController.popBackStack() }
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
