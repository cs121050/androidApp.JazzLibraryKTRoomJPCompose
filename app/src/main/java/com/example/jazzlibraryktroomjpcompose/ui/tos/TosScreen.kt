// app/src/main/java/com/example/jazzlibraryktroomjpcompose/ui/tos/TosScreen.kt

package com.example.jazzlibraryktroomjpcompose.ui.tos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TosScreen(
    viewModel: TosViewModel = hiltViewModel(),
    onAccept: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var allAccepted by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Jazz Library",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please review and accept our policies to continue",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Privacy Policy Section
            PolicySection(
                title = "📋 Privacy Policy",
                content = """
                    We are committed to protecting your privacy. This app:
                    
                    • Does NOT collect personal data without your consent
                    • Does NOT sell or share your data with third parties
                    • Uses YouTube API to fetch music and videos
                    • Stores your preferences locally on your device
                    • May use analytics for crash reporting (Firebase Crashlytics)
                    
                    For full details, visit our privacy policy page.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms of Service Section
            PolicySection(
                title = "📜 Terms of Service",
                content = """
                    By using this app, you agree to:
                    
                    • Use the app only for personal, non-commercial purposes
                    • Not reverse engineer or modify the app
                    • Respect intellectual property rights
                    • Not use the app for illegal activities
                    • Not harass other users or misuse the platform
                    
                    We reserve the right to update these terms at any time.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // YouTube ToS Section
            PolicySection(
                title = "🎵 YouTube API Terms",
                content = """
                    This app uses the YouTube API and is subject to:
                    
                    • YouTube's Terms of Service
                    • YouTube's Community Guidelines
                    • Respectful use of YouTube content
                    • No caching or downloading of videos
                    • Proper attribution of content
                    
                    You acknowledge that you will abide by YouTube's policies
                    when using this app to search and play music.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data Collection Disclosure
            PolicySection(
                title = "📊 Data Collection",
                content = """
                    We collect minimal data:
                    
                    • App crashes (via Firebase Crashlytics)
                    • Anonymous usage statistics
                    • Your local preferences (stored on device)
                    
                    This helps us improve the app and fix bugs.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Accept/Reject Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        allAccepted = true
                        viewModel.acceptAllPolicies()
                        onAccept()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "✓ Accept All Policies",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.rejectPolicies()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "✗ Reject & Exit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "You must accept all policies to use this app.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}