package com.taskserver.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.taskserver.app.R
import com.taskserver.app.data.settings.SettingsManager
import com.taskserver.app.ui.screens.home.HomeScreen
import com.taskserver.app.ui.screens.logs.LogsScreen
import com.taskserver.app.ui.screens.servers.ServerEditScreen
import com.taskserver.app.ui.screens.servers.ServersScreen
import com.taskserver.app.ui.screens.settings.SettingsScreen
import com.taskserver.app.ui.screens.ssh.SshScreen
import com.taskserver.app.ui.screens.tasks.TaskBuilderScreen
import com.taskserver.app.ui.screens.tasks.TasksScreen
import com.taskserver.app.ui.screens.terminal.TerminalScreen
import com.taskserver.app.ui.theme.*

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Home    : Screen("home",    R.string.nav_home,    Icons.Filled.Home)
    object Servers : Screen("servers", R.string.nav_servers, Icons.Filled.Dns)
    object Ssh     : Screen("ssh",     R.string.nav_ssh,     Icons.Filled.Terminal)
    object Tasks   : Screen("tasks",   R.string.nav_tasks,   Icons.Filled.Bolt)
    object Logs    : Screen("logs",    R.string.nav_logs,    Icons.Filled.History)
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                shape = androidx.compose.ui.graphics.RectangleShape
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .height(64.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                val accentColor = if (screen == Screen.Tasks) PurpleSecondary else CyanPrimary
                                val scale by animateFloatAsState(
                                    targetValue = if (selected) 1.08f else 1.0f,
                                    animationSpec = tween(180),
                                    label = "iconScale"
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = accentColor),
                                            onClick = {
                                                if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (selected) accentColor.copy(alpha = 0.14f) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = stringResource(screen.labelRes),
                                            modifier = Modifier
                                                .size(22.dp)
                                                .scale(scale),
                                            tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(screen.labelRes),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
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
