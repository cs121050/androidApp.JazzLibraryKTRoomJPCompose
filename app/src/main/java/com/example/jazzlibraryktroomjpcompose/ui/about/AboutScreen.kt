package com.example.jazzlibraryktroomjpcompose.ui.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import com.example.jazzlibraryktroomjpcompose.ui.update.UpdateManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val updateManager = remember { UpdateManager(context) }
    var manualUpdateInProgress by remember { mutableStateOf(false) }

    val appVersion = remember { getAppVersion(context) }
    val lastUpdateDate = remember { getLastUpdateDate(context) }

    BackHandler { onNavigateBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("About") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Version info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("App Version")
                        }
                        Text(appVersion, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Last Updated")
                        }
                        Text(lastUpdateDate, fontWeight = FontWeight.Bold)
                    }
                }
            }




            // Mission statement (Jamey Aebersold quote)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎵 Why This App Was Created", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"Oh my, they don't listen to jazz like they should. They're missing the history—not just Dixieland, the swing era, blah blah blah—but they just… I've been to a couple of jazz festivals in the last couple of years, and I have a feeling that they don't know the players. If you say 'Miles Davis' to them, it might ring a bell, but if you say 'Horace Silver,' 'Hank Mobley,' or even 'Sonny Rollins,' they don't know who they are. You mention 'Blue Bossa'—they've never heard of the tune. And they don't know the records *Maiden Voyage*, *Kind of Blue*—totally unfamiliar. And they don't really know who I am, or David Baker, or Jerry Coker, or Dan Haerle.\n\nI don't know what's going on. They're playing what they call 'jazz groups,' 'stage bands,' 'big bands,' but they don't know the history. So when they stand up to play, it doesn't make a lot of sense sometimes. But as they spend time listening, they end up playing better. I think it falls back on the colleges and band directors. They often don't know—oftentimes— nothing about improvisation or teaching improvisation, so consequently they go into a big band with 18 or 20 kids, and there will be very little real improvisation going on. So we need to educate the educators, and that is how it has been for the last 30 years.\" — Jamey Aebersold\n\nIn that interview, Aebersold said the biggest problem with jazz students today is that they don't know the tradition—they don't listen to the jazz albums of the legends who created this music.\n\nThis app was created to solve that problem.\n\nIt is meant for jazz students to use in the breaks between practice sessions, instead of doomscrolling on social media. That way, valuable time is saved and repurposed back to music. Practice sessions become much more efficient, and the user of this app gains traditional knowledge and lore—and a chance to hear the music of the masters.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            }

            // Manual update button
            Button(
                onClick = {
                    manualUpdateInProgress = true
                    updateManager.openDownloadUrl()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = !manualUpdateInProgress
            ) {
                if (manualUpdateInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Check for Update")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun getAppVersion(context: Context): String {
    return try {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "Unknown"
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        "$versionName (build $versionCode)"
    } catch (e: Exception) { "Unknown" }
}

private fun getLastUpdateDate(context: Context): String {
    return try {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val date = Date(packageInfo.lastUpdateTime)
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) { "Unknown" }
}