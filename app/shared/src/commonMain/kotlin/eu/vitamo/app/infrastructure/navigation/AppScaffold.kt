package eu.vitamo.app.infrastructure.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class DrawerDestination(
    val title: String,
    val destination: MainDestination,
)

private val drawerDestinations = listOf(
    DrawerDestination(
        title = "Home",
        destination = MainDestination.Home,
    ),
    DrawerDestination(
        title = "Instellingen",
        destination = MainDestination.Settings,
    ),
    DrawerDestination(
        title = "Profile",
        destination = MainDestination.Profile
    ),
    DrawerDestination(
        title = "Users",
        destination = MainDestination.Users
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    currentDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
    onLogoutClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed,
    )
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 20.dp,
                    ),
                )

                drawerDestinations.forEach { drawerDestination ->
                    NavigationDrawerItem(
                        label = {
                            Text(drawerDestination.title)
                        },
                        selected = currentDestination.drawerRoot() == drawerDestination.destination,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }

                            if (
                                currentDestination.drawerRoot() !=
                                drawerDestination.destination
                            ) {
                                onDestinationSelected(
                                    drawerDestination.destination,
                                )
                            }
                        },
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                        ),
                    )
                }
                Spacer(modifier.weight(1f))
                OutlinedButton(
                    modifier= Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                    onClick = onLogoutClicked,
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Red,
                        disabledContentColor = Color.Gray,
                        disabledContainerColor = Color.Gray)) {
                    Text(
                        modifier = modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Logout")
                }
            }
        },
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(currentDestination.navigationTitle())
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                        ) {
                            // Geen extra Material Icons dependency nodig.
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    },
                )
            },
            content = content,
        )
    }
}

/**
 * Koppelt onderliggende feedbestemmingen aan het Feed-item
 * in de navigation drawer.
 */
private fun MainDestination.drawerRoot(): MainDestination =
    when (this) {
        MainDestination.Home -> MainDestination.Home
        MainDestination.Profile -> MainDestination.Profile
        MainDestination.Settings -> MainDestination.Settings
        MainDestination.Users -> MainDestination.Users
        is MainDestination.FriendRequests -> this
    }

private fun MainDestination.navigationTitle(): String = title