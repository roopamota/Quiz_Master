package com.example.quizmaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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

class QuizIntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subject = intent.getStringExtra("subject") ?: "Unknown"

        setContent {
            QuizMasterTheme {
                QuizIntroScreen(subject)
            }
        }
    }
}

@Composable
fun QuizIntroScreen(subject: String) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)) // Blue gradient
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Quiz: $subject", style = MaterialTheme.typography.headlineSmall, color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))
            Text("Instructions:", fontSize = 18.sp, color = Color.White)
            Text("• You will get 5 questions.", fontSize = 16.sp, color = Color.White)
            Text("• Time limit: 2 minutes.", fontSize = 16.sp, color = Color.White)
            Text("• No negative marking.", fontSize = 16.sp, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(context, QuizActivity::class.java)
                    intent.putExtra("subject", subject)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Quiz")
            }
        }
    }
}
