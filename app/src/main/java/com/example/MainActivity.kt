package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.mesgaging.ui.MainChatScreen
import com.example.mesgaging.ui.MesgagingViewModel
import com.example.mesgaging.ui.components.GoogleSignInScreen
import com.example.mesgaging.ui.components.StartupBirdOverlay
import com.example.mesgaging.ui.components.attemptAutoSignIn
import com.example.ui.theme.MesgagingTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: MesgagingViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()

        // Attempt silent Google auto-sign-in on startup
        val credentialManager = CredentialManager.create(this)
        attemptAutoSignIn(
            context = this,
            credentialManager = credentialManager,
            onAuthSuccess = {
                viewModel.onUserSignedIn(Firebase.auth.currentUser)
            },
            onUnauthenticated = {
                // Shows Google Sign-In Gate
            },
            scope = lifecycleScope
        )

        setContent {
            MesgagingTheme {
                var isStartupFinished by remember { mutableStateOf(false) }
                var firebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        firebaseUser = auth.currentUser
                        if (auth.currentUser != null) {
                            viewModel.onUserSignedIn(auth.currentUser)
                        }
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (firebaseUser == null) {
                        GoogleSignInScreen(
                            onAuthSuccess = {
                                firebaseUser = Firebase.auth.currentUser
                                viewModel.onUserSignedIn(Firebase.auth.currentUser)
                            }
                        )
                    } else {
                        MainChatScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!isStartupFinished) {
                        StartupBirdOverlay(
                            onAnimationComplete = {
                                isStartupFinished = true
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
