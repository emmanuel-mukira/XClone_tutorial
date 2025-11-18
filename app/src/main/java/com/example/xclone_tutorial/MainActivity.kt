package com.example.xclone_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xclone_tutorial.ui.auth.LoginScreen
import com.example.xclone_tutorial.ui.auth.SignUpScreen
import com.example.xclone_tutorial.ui.home.HomeScreen
import com.example.xclone_tutorial.ui.theme.XClone_tutorialTheme
import com.example.xclone_tutorial.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            XClone_tutorialTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Shared AuthViewModel across auth screens
    val authViewModel: AuthViewModel = viewModel()
    
    var currentScreen by remember { mutableStateOf(
        if (authViewModel.currentUser != null) Screen.Home else Screen.Login
    ) }

    when (currentScreen) {
        Screen.SignUp -> SignUpScreen(
            onSignUpSuccess = { currentScreen = Screen.Home },
            onNavigateToLogin = { currentScreen = Screen.Login },
            viewModel = authViewModel
        )
        Screen.Login -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.Home },
            onNavigateToSignUp = { currentScreen = Screen.SignUp },
            viewModel = authViewModel
        )
        Screen.Home -> HomeScreen(
            onLogout = {
                authViewModel.logout()
                currentScreen = Screen.Login
            },
            onLogin = { currentScreen = Screen.Login },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}

enum class Screen {
    SignUp, Login, Home
}