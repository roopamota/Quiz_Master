package com.example.quizmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizmaster.ui.theme.QuizMasterTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

// -------------------------- UI COMPOSABLES --------------------------

@Composable
fun QuizScreen(subject: String) {
    val questions = listOf(
        QuizQuestion(
            question = "What is Kotlin?",
            options = listOf("A language", "A planet", "A car", "A framework"),
            correctAnswer = 0
        ),
        QuizQuestion(
            question = "What does OOP stand for?",
            options = listOf("Only One Process", "Object Oriented Programming", "Open Office Project", "Object Overload Principle"),
            correctAnswer = 1
        ),
        QuizQuestion(
            question = "What is a Python dictionary?",
            options = listOf("A book", "A variable", "A key-value store", "A snake data type"),
            correctAnswer = 2
        ),
        QuizQuestion(
            question = "What is Machine Learning?",
            options = listOf("A cooking method", "An AI technique", "A machine part", "A language"),
            correctAnswer = 1
        ),
        QuizQuestion(
            question = "What is the extension of a Kotlin file?",
            options = listOf(".java", ".kt", ".kotlin", ".xml"),
            correctAnswer = 1
        )
    )

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var timer by remember { mutableIntStateOf(10) } // 10 seconds per question
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = currentIndex) {
        selectedAnswer = -1
        timer = 10
        scope.launch {
            while (timer > 0) {
                delay(1000)
                timer--
            }
            if (timer == 0) {
                currentIndex++
            }
        }
    }

    if (currentIndex < questions.size) {
        val question = questions[currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            LinearProgressIndicator(
                progress = (currentIndex + 1) / questions.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Subject: $subject", fontSize = 20.sp)
            Text("Time left: $timer sec", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text("Q${currentIndex + 1}: ${question.question}", fontSize = 18.sp)

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
                    currentIndex++
                },
                enabled = selectedAnswer != -1
            ) {
                Text("Next")
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("🎉 Quiz Complete!", fontSize = 24.sp)
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
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// -------------------------- DATA MODEL --------------------------

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)
