package com.seance.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.seance.tv.ui.auth.AuthScreen
import com.seance.tv.ui.auth.AuthViewModel
import com.seance.tv.ui.home.HomeScreen
import com.seance.tv.ui.theme.SeanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeanceTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()

                when {
                    authState.isAuthenticated -> HomeScreen()
                    else -> AuthScreen(viewModel = authViewModel)
                }
            }
        }
    }
}
