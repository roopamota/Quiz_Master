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
    val allQuestions = mapOf(
        "Python" to listOf(
            QuizQuestion("What is the correct file extension for Python files?", listOf(".py", ".pt", ".pyt", ".pyth"), 0),
            QuizQuestion("Which keyword is used for function in Python?", listOf("func", "define", "def", "function"), 2),
            QuizQuestion("Which data type is used to store True or False?", listOf("int", "str", "bool", "float"), 2),
        ),
        "OOPs" to listOf(
            QuizQuestion("OOP stands for?", listOf("Object Oriented Programming", "Operator On Point", "Optical Operation", "Ordered Oriented Procedure"), 0),
            QuizQuestion("Which of the following is not a pillar of OOP?", listOf("Encapsulation", "Polymorphism", "Abstraction", "Compilation"), 3),
            QuizQuestion("What is inheritance?", listOf("A way to inherit money", "A way to reuse code", "A loop concept", "A variable type"), 1),
        ),
        "Machine Learning" to listOf(
            QuizQuestion("ML is a subset of?", listOf("Math", "AI", "Physics", "Robotics"), 1),
            QuizQuestion("Which of these is a ML algorithm?", listOf("Linear Regression", "Sorting", "Searching", "Merging"), 0),
            QuizQuestion("What is overfitting?", listOf("When a model is too simple", "When a model fits the training data too well", "A type of neural network", "None"), 1),
        )
    )

    val context = LocalContext.current
    val questions = allQuestions[subject] ?: emptyList()

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var timer by remember { mutableIntStateOf(10) }
    var score by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }
    var totalTime by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentIndex, quizCompleted) {
        if (!quizCompleted && currentIndex < questions.size) {
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            )
            .padding(24.dp)
    ) {
        if (!quizCompleted && currentIndex < questions.size) {
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

// ------------------------ DATA MODELS ------------------------

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
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
