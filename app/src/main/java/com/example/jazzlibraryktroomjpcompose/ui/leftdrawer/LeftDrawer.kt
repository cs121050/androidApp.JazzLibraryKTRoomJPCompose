package com.example.jazzlibraryktroomjpcompose.ui.leftdrawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.jazzlibraryktroomjpcompose.domain.models.AuthState
import com.example.jazzlibraryktroomjpcompose.domain.models.User
import com.example.jazzlibraryktroomjpcompose.ui.auth.AuthViewModel

@Composable
fun LeftDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onRefreshClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAboutClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = when (authState) {
        is AuthState.Authenticated -> (authState as AuthState.Authenticated).user
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Do nothing – just consume the click
            }
    ) {
        // Top item
        UserProfileHeader(currentUser = currentUser)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            throw RuntimeException("Test crash from Crashlytics")
        }) {
            Text("Test Crash")
        }

        // Build menu items in desired order
        val menuItems = buildMenuItems(
            currentUser = currentUser,
            onAboutClick = { onAboutClick() },
            onRefreshClick = { onRefreshClick(); onClose() },
            onClearHistoryClick = { onClearHistoryClick(); onClose() },
            onLoginOrSignOut = {
                if (currentUser != null) {
                    authViewModel.signOut()
                } else {
                    onLoginClick()
                }
                onClose()
            }
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(menuItems) { item ->
                MenuItemRow(item = item)
            }
        }
    }
}

/**
 * Builds the menu list with Login/Sign Out placed right after "Clear History"
 */
private fun buildMenuItems(
    currentUser: User?,
    onRefreshClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onLoginOrSignOut: () -> Unit,
    onAboutClick: () -> Unit
): List<MenuItem> {
    val items = mutableListOf<MenuItem>()

    // 3. Login or Sign Out (right after Clear History)
    if (currentUser != null) {
        items.add(MenuItem("Sign Out", Icons.Default.Logout, onLoginOrSignOut))
    } else {
        items.add(MenuItem("Login / Sign Up", Icons.Default.Login, onLoginOrSignOut))
    }

    // 1. Refresh Data
    items.add(MenuItem("Refresh Data", Icons.Default.Refresh, onRefreshClick))
    // 2. Clear History
    items.add(MenuItem("Clear History", Icons.Default.History, onClearHistoryClick))


    // Disabled items (no implementation yet)
    items.add(MenuItem("Bookmarks", Icons.Default.Bookmark, { }, enabled = false))
    items.add(MenuItem("Playlists", Icons.Default.PlaylistPlay, { }, enabled = false))
    items.add(MenuItem("Settings", Icons.Default.Settings, { }, enabled = false))

    items.add(MenuItem("About", Icons.Default.Info, onAboutClick))
    return items
}

/**
 * User profile area (avatar + name/status) – top-left of drawer
 * - When logged in: shows user avatar (photo or placeholder) and name
 * - When not logged in: empty circle (no icon) and "Not logged in" text
 */
@Composable
private fun UserProfileHeader(currentUser: User?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar circle: if logged in, show photo or empty colored circle; if not logged in, empty circle (no icon)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (currentUser != null && currentUser.photoUrl != null) {
                AsyncImage(
                    model = currentUser.photoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            // No icon when not logged in, and no icon when logged in without photoUrl (only background)
        }

        Column {
            if (currentUser != null) {
                Text(
                    text = currentUser.displayName ?: currentUser.email ?: "User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (currentUser.isAnonymous) {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Not logged in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class MenuItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true  // ← new property, default true
)

@Composable
fun MenuItemRow(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (item.enabled) {
                    modifier.clickable { item.onClick() }
                } else {
                    modifier
                }
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (item.enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium,
            color = if (item.enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        )
    }
}