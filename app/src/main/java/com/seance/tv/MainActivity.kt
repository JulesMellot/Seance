package com.seance.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.seance.tv.ui.auth.AuthPhase
import com.seance.tv.ui.auth.AuthScreen
import com.seance.tv.ui.auth.AuthViewModel
import com.seance.tv.ui.auth.ProfileScreen
import com.seance.tv.ui.navigation.AppScaffold
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

                when (authState.phase) {
                    AuthPhase.Ready -> AppScaffold(profileThumb = authState.activeProfileThumb)
                    AuthPhase.Profiles -> ProfileScreen(
                        profiles = authState.profiles,
                        isLoading = authState.isLoading,
                        error = authState.error,
                        onSelect = { user, pin -> authViewModel.selectProfile(user, pin) }
                    )
                    else -> AuthScreen(viewModel = authViewModel)
                }
            }
        }
    }
}
