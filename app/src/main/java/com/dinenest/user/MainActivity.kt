package com.dinenest.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dinenest.user.data.ThemePrefs
import com.dinenest.user.model.FoodMall
import com.dinenest.user.ui.theme.DineNestTheme
import com.dinenest.user.ui.theme.LoginScreen
import com.dinenest.user.ui.theme.MainScreen
import com.dinenest.user.ui.theme.MallDetailsScreen
import com.dinenest.user.ui.theme.RegisterScreen
import com.dinenest.user.ui.theme.VerifyEmailScreen
import com.google.firebase.auth.FirebaseAuth

enum class AuthStage { LOGIN, REGISTER, VERIFY, HOME }

private fun initialStage(): AuthStage {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return AuthStage.LOGIN
    if (user.email.isNullOrEmpty()) {
        auth.signOut()
        return AuthStage.LOGIN
    }
    return if (user.isEmailVerified) AuthStage.HOME else AuthStage.VERIFY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkMode by remember {
                mutableStateOf(ThemePrefs.load(this@MainActivity, systemDark))
            }

            DisposableEffect(darkMode) {
                val barStyle = if (darkMode) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose { }
            }

            DineNestTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var stage by remember { mutableStateOf(initialStage()) }
                    var selectedMall by remember { mutableStateOf<FoodMall?>(null) }

                    DisposableEffect(Unit) {
                        val auth = FirebaseAuth.getInstance()
                        val listener = FirebaseAuth.AuthStateListener {
                            if (it.currentUser == null) {
                                selectedMall = null
                                if (stage != AuthStage.REGISTER) stage = AuthStage.LOGIN
                            }
                        }
                        auth.addAuthStateListener(listener)
                        onDispose { auth.removeAuthStateListener(listener) }
                    }

                    when (stage) {
                        AuthStage.LOGIN -> LoginScreen(
                            onLoggedIn = { stage = AuthStage.HOME },
                            onNeedsVerification = { stage = AuthStage.VERIFY },
                            onRegisterClick = { stage = AuthStage.REGISTER }
                        )

                        AuthStage.REGISTER -> {
                            BackHandler { stage = AuthStage.LOGIN }
                            RegisterScreen(
                                onRegistered = { stage = AuthStage.VERIFY },
                                onLoginClick = { stage = AuthStage.LOGIN }
                            )
                        }

                        AuthStage.VERIFY -> VerifyEmailScreen(
                            onVerified = { stage = AuthStage.HOME },
                            onBackToLogin = { stage = AuthStage.LOGIN }
                        )

                        AuthStage.HOME -> {
                            val mall = selectedMall
                            if (mall == null) {
                                MainScreen(
                                    onMallClick = { selectedMall = it },
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        selectedMall = null
                                        stage = AuthStage.LOGIN
                                    },
                                    darkMode = darkMode,
                                    onDarkModeChange = {
                                        darkMode = it
                                        ThemePrefs.save(this@MainActivity, it)
                                    }
                                )
                            } else {
                                BackHandler { selectedMall = null }
                                MallDetailsScreen(mall = mall, onBack = { selectedMall = null })
                            }
                        }
                    }
                }
            }
        }
    }
}