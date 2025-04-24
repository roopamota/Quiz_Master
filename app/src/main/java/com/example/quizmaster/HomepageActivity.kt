package com.example.quizmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.quizmaster.ui.theme.QuizMasterTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomepageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizMasterTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            ),
        content = content
    )
}

@Composable
fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Menu") },
                        label = { Text("Menu") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard") },
                        label = { Text("Leaderboard") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> MenuScreen()
                    1 -> LeaderboardScreen()
                    2 -> ProfileScreen()
                }
            }
        }
    }
}


@Composable
fun MenuScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top AppBar content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Select a Subject",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Privacy Policy") },
                        onClick = {
                            expanded = false
                            showPrivacyDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sign Out") },
                        onClick = {
                            expanded = false
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        // Subject list
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuizCategory("Python")
            QuizCategory("OOPs")
            QuizCategory("Machine Learning")
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Privacy Policy") },
                text = {
                    Text(
                        "This app collects no personal data. Your quiz results are stored anonymously in the leaderboard. " +
                                "We do not share your information with any third party."
                    )
                }
            )
        }
    }
}






@Composable
fun QuizCategory(name: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, QuizIntroActivity::class.java)
                intent.putExtra("subject", name)
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2196F3))
        }
    }
}

@Composable
fun LeaderboardScreen() {
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val firestore = FirebaseFirestore.getInstance()

    // Fetch leaderboard data directly from "leaderboard" collection
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()




    entries = snapshot.documents.mapNotNull { it.toObject(LeaderboardEntry::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (loading) {
            CircularProgressIndicator(color = Color.White)
        } else if (entries.isEmpty()) {
            Text(
                text = "No scores yet. Take a quiz to get started!",
                color = Color.White.copy(alpha = 0.8f)
            )
        } else {
            entries.forEachIndexed { index, entry ->
                LeaderboardRow(entry = entry, rank = index + 1)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@Composable
fun LeaderboardRow(entry: LeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("#$rank - ${entry.subject}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Score: ${entry.score}/${entry.total} • Time: ${entry.timeTakenSeconds}s", fontSize = 16.sp)
        }
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Profile", style = MaterialTheme.typography.headlineSmall, color = Color.White)

        ProfileItem(label = "Name", value = "Roopa Mota")
        ProfileItem(label = "Email", value = "roopamota260@gmail.com")
        ProfileItem(label = "Total Quizzes Taken", value = "${LeaderboardManager.entries.size}")
        ProfileItem(label = "Highest Score", value = "${LeaderboardManager.entries.maxOfOrNull { it.score } ?: 0}")
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 16.sp)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}







