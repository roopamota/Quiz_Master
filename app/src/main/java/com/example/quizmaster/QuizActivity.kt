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

    val questions = allQuestions[subject] ?: emptyList()

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var timer by remember { mutableIntStateOf(10) }
    var score by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = currentIndex, key2 = quizCompleted) {
        if (!quizCompleted && currentIndex < questions.size) {
            selectedAnswer = -1
            timer = 10
            scope.launch {
                while (timer > 0) {
                    delay(1000)
                    timer--
                }
                if (timer == 0) {
                    currentIndex++
                    if (currentIndex >= questions.size) quizCompleted = true
                }
            }
        }
    }

    if (!quizCompleted && currentIndex < questions.size) {
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
                    if (selectedAnswer == question.correctAnswer) score++
                    currentIndex++
                    if (currentIndex >= questions.size) quizCompleted = true
                },
                enabled = selectedAnswer != -1
            ) {
                Text("Next")
            }
        }
    } else if (quizCompleted) {
        val percentage = (score.toFloat() / questions.size.toFloat()) * 100
        val passed = percentage >= 50

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (passed) " Test Passed!" else " Test Failed!",
                fontSize = 24.sp,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Your Score: $score/${questions.size} (${"%.1f".format(percentage)}%)", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                currentIndex = 0
                score = 0
                quizCompleted = false
            }) {
                Text("Retake Quiz")
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
