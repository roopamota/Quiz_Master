package com.example.quizmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizmaster.ui.theme.QuizMasterTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subject = intent.getStringExtra("subject") ?: "General"

        setContent {
            QuizMasterTheme {
                QuizScreen(subject)
            }
        }
    }
}

@Composable
fun QuizScreen(subject: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var timer by remember { mutableIntStateOf(10) }
    var score by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }
    var totalTime by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }

    // Fetch questions from Firestore
    LaunchedEffect(subject) {
        firestore.collection("quizzes")
            .document(subject)
            .collection("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                questions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)
                }.shuffled()
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    LaunchedEffect(currentIndex, quizCompleted, questions) {
        if (!quizCompleted && questions.isNotEmpty() && currentIndex < questions.size) {
            selectedAnswer = -1
            timer = 10
            scope.launch {
                while (timer > 0) {
                    delay(1000)
                    timer--
                    totalTime++
                }
                if (timer == 0) {
                    currentIndex++
                    if (currentIndex >= questions.size) quizCompleted = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF64B5F6))))
            .padding(24.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else if (!quizCompleted && currentIndex < questions.size) {
            val question = questions[currentIndex]

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxSize()
            ) {
                LinearProgressIndicator(
                    progress = (currentIndex + 1) / questions.size.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Text("Subject: $subject", fontSize = 20.sp, color = Color.White)
                Text("Time left: $timer sec", fontSize = 16.sp, color = Color.White)
                Text("Q${currentIndex + 1}: ${question.question}", fontSize = 18.sp, color = Color.White)

                question.options.forEachIndexed { index, option ->
                    AnswerOption(
                        text = option,
                        selected = selectedAnswer == index,
                        onClick = { selectedAnswer = index }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (selectedAnswer == question.correctAnswer) score++
                        currentIndex++
                        if (currentIndex >= questions.size) quizCompleted = true
                    },
                    enabled = selectedAnswer != -1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Next", color = Color(0xFF2196F3))
                }
            }
        } else if (quizCompleted) {
            val percentage = (score.toFloat() / questions.size.toFloat()) * 100
            val passed = percentage >= 50

            // Save to leaderboard
            LaunchedEffect(Unit) {
                LeaderboardManager.entries.add(
                    LeaderboardEntry(subject, score, questions.size, totalTime)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (passed) "🎉 Test Passed!" else "❌ Test Failed!",
                    fontSize = 24.sp,
                    color = if (passed) Color.White else Color.Red
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Your Score: $score/${questions.size} (${String.format(Locale.getDefault(), "%.1f", percentage)}%)",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text("Time Taken: ${totalTime}s", fontSize = 16.sp, color = Color.White)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        currentIndex = 0
                        score = 0
                        quizCompleted = false
                        totalTime = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Retake Quiz", color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, HomepageActivity::class.java)
                        intent.putExtra("openTab", 1)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Go to Leaderboard", color = Color(0xFF2196F3))
                }
            }
        }
    }
}

@Composable
fun AnswerOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color.White.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp),
            color = if (selected) Color.White else Color.Black
        )
    }
}

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0
)

data class LeaderboardEntry(
    val subject: String,
    val score: Int,
    val total: Int,
    val timeTakenSeconds: Int
)

object LeaderboardManager {
    val entries = mutableStateListOf<LeaderboardEntry>()
}
