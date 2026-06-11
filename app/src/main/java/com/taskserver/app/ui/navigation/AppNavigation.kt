package com.taskserver.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.taskserver.app.data.settings.SettingsManager
import com.taskserver.app.ui.screens.home.HomeScreen
import com.taskserver.app.ui.screens.logs.LogsScreen
import com.taskserver.app.ui.screens.servers.ServerEditScreen
import com.taskserver.app.ui.screens.servers.ServersScreen
import com.taskserver.app.ui.screens.ssh.SshScreen
import com.taskserver.app.ui.screens.settings.SettingsScreen
import com.taskserver.app.ui.screens.tasks.TaskBuilderScreen
import com.taskserver.app.ui.screens.tasks.TasksScreen
import com.taskserver.app.ui.screens.terminal.TerminalScreen
import com.taskserver.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home    : Screen("home",    "Home",    Icons.Filled.Home)
    object Servers : Screen("servers", "Server",  Icons.Filled.Dns)
    object Ssh     : Screen("ssh",     "SSH",     Icons.Filled.Terminal)
    object Tasks   : Screen("tasks",   "Task",    Icons.Filled.Bolt)
    object Logs    : Screen("logs",    "Log",     Icons.Filled.History)
}

val bottomNavItems = listOf(Screen.Home, Screen.Servers, Screen.Ssh, Screen.Tasks, Screen.Logs)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    settingsManager: SettingsManager,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = bottomNavItems.map { it.route }
    val showBottomBar = bottomRoutes.any { currentRoute == it }

    TaskServerTheme(darkTheme = isDarkMode) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            // Fix: Properly clear stack when returning Home or switching
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        tint = if (selected) {
                                            if (screen == Screen.Tasks) PurpleSecondary else CyanPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) {
                                            if (screen == Screen.Tasks) PurpleSecondary else CyanPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = (if (screen == Screen.Tasks) PurpleSecondary else CyanPrimary).copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(220)) + slideInHorizontally { it / 8 } },
                exitTransition  = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition  = { fadeIn(animationSpec = tween(220)) },
                popExitTransition   = { fadeOut(animationSpec = tween(150)) + slideOutHorizontally { it / 8 } }
            ) {
                composable(Screen.Home.route)    { HomeScreen(navController) }
                composable(Screen.Servers.route) { ServersScreen(navController) }
                composable(Screen.Ssh.route)     { SshScreen(navController) }
                composable(Screen.Tasks.route)   { TasksScreen(navController) }
                composable(Screen.Logs.route)    { LogsScreen(navController) }
                
                composable("settings") { 
                    SettingsScreen(
                        navController = navController,
                        onThemeToggle = onThemeToggle
                    ) 
                }

                composable(
                    route = "server_edit?serverId={serverId}",
                    arguments = listOf(navArgument("serverId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStack ->
                    val serverId = backStack.arguments?.getLong("serverId") ?: -1L
                    ServerEditScreen(navController = navController, serverId = serverId)
                }

                composable(
                    route = "task_builder?taskId={taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStack ->
                    val taskId = backStack.arguments?.getLong("taskId") ?: -1L
                    TaskBuilderScreen(navController = navController, taskId = taskId)
                }

                composable(
                    route = "terminal/{taskId}/{serverId}",
                    arguments = listOf(
                        navArgument("serverId") { type = NavType.LongType },
                        navArgument("taskId")   { type = NavType.LongType }
                    )
                ) { backStack ->
                    val serverId = backStack.arguments!!.getLong("serverId")
                    val taskId   = backStack.arguments!!.getLong("taskId")
                    TerminalScreen(navController = navController, taskId = taskId, serverId = serverId)
                }
            }
        }
    }
}
