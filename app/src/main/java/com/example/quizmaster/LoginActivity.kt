package com.example.quizmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.PatternsCompat
import com.example.quizmaster.ui.theme.QuizMasterTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizMasterTheme {
                LoginScreen { email, password ->
                    if (validateInput(email, password)) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3)) // Green to Blue gradient
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White) // Card effect for form
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = !PatternsCompat.EMAIL_ADDRESS.matcher(it).matches()
                },
                label = { Text("Email") },
                isError = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError) {
                Text(text = "Invalid Email", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text("Password") },
                isError = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError) {
                Text(text = "Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SIGN UP BUTTON WITH BOLD TEXT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically // Fix alignment issue
            ) {
                Text(text = "Don't have an account?", fontSize = 16.sp, color = Color.Gray)

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, SignUpActivity::class.java))
                    },
                    contentPadding = PaddingValues(0.dp) // Fix padding issue
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueSecondary
                    )
                }
            }

        }
    }
}

// Define theme colors
val GreenPrimary = Color(0xFF4CAF50) // Green
val BlueSecondary = Color(0xFF2196F3) // Blue

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    QuizMasterTheme {
        LoginScreen { _, _ -> }
    }
}
