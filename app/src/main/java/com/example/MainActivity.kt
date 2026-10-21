package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.FrontCraftViewModel
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.BatchesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExamsScreen
import com.example.ui.screens.FacultyScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.FrontCraftPROTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FrontCraftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrontCraftPROTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: FrontCraftViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle back button to return to Dashboard if in other tabs
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.switchTab(AppTab.DASHBOARD)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyPrimary)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "FRONTCRAFT PRO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Coaching Institute Management",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = NavySecondary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Logged in: ${currentUser.fullName} (${currentUser.role})",
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation items
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val icon = when (tab) {
                        AppTab.DASHBOARD -> Icons.Default.Speed
                        AppTab.BATCHES -> Icons.Default.Class
                        AppTab.ATTENDANCE -> Icons.Default.CheckCircle
                        AppTab.STUDENTS -> Icons.Default.Groups
                        AppTab.FACULTY -> Icons.Default.School
                        AppTab.FEES -> Icons.Default.CurrencyRupee
                        AppTab.EXAMS -> Icons.Default.MenuBook
                        AppTab.REPORTS -> Icons.Default.FileDownload
                        AppTab.ANNOUNCEMENTS -> Icons.Default.Campaign
                        AppTab.SETTINGS -> Icons.Default.Settings
                    }

                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = tab.title, tint = if (isSelected) GoldAccent else NavyPrimary) },
                        label = { Text(tab.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        selected = isSelected,
                        onClick = {
                            viewModel.switchTab(tab)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NavyPrimary,
                            selectedTextColor = Color.White,
                            unselectedTextColor = NavyPrimary
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("nav_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "FrontCraft PRO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = currentTab.title,
                                fontSize = 11.sp,
                                color = GoldAccent
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        // Role Pill & Fast Switcher
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentUser.role,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val nextRole = when (currentUser.role) {
                                    "ADMIN" -> "TEACHER"
                                    "TEACHER" -> "STUDENT"
                                    "STUDENT" -> "PARENT"
                                    else -> "ADMIN"
                                }
                                viewModel.switchRole(nextRole)
                            },
                            modifier = Modifier.testTag("switch_role_top_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Role", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyPrimary,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // Primary 4 tabs for quick mobile access + Drawer for all
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 6.dp
                ) {
                    val bottomTabs = listOf(
                        AppTab.DASHBOARD to Icons.Default.Speed,
                        AppTab.BATCHES to Icons.Default.Class,
                        AppTab.ATTENDANCE to Icons.Default.CheckCircle,
                        AppTab.STUDENTS to Icons.Default.Groups,
                        AppTab.FEES to Icons.Default.CurrencyRupee
                    )

                    bottomTabs.forEach { (tab, icon) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            selected = isSelected,
                            onClick = { viewModel.switchTab(tab) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyPrimary,
                                selectedTextColor = NavyPrimary,
                                indicatorColor = GoldAccent.copy(alpha = 0.35f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("bottom_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppTab.BATCHES -> BatchesScreen(viewModel = viewModel)
                    AppTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                    AppTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                    AppTab.FACULTY -> FacultyScreen(viewModel = viewModel)
                    AppTab.FEES -> FeesScreen(viewModel = viewModel)
                    AppTab.EXAMS -> ExamsScreen(viewModel = viewModel)
                    AppTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    AppTab.ANNOUNCEMENTS -> AnnouncementsScreen(viewModel = viewModel)
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
