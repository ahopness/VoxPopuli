package dev.lucasangelo.voxpopuli.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.lucasangelo.voxpopuli.ui.screen.HomeRoute
import dev.lucasangelo.voxpopuli.ui.screen.HomeScreen
import dev.lucasangelo.voxpopuli.ui.screen.OnboardingRoute
import dev.lucasangelo.voxpopuli.ui.screen.OnboardingScreen
import dev.lucasangelo.voxpopuli.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun App(
    viewModel: AppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val snackbarScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showMessage: (String) -> Unit = { message ->
        snackbarScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    if (settings == null) return

    Box {
        NavHost(
            navController,
            startDestination = if (settings!!.showOnboarding) OnboardingRoute else HomeRoute,
            enterTransition = { slideIntoContainer(
                animationSpec = tween(200, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            ) },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = { slideOutOfContainer(
                animationSpec = tween(200, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            ) }
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    navController,
                )
            }
            composable<HomeRoute> {
                HomeScreen(
                    navController,
                    showMessage
                )
            }
        }

        SnackbarHost(
            snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
        )
    }
}