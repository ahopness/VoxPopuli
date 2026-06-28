package dev.lucasangelo.voxpopuli.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Composable
fun OnboardingScreen(
    rootNavController: NavController,
    rootShowMessage: (String) -> Unit,
) {

}