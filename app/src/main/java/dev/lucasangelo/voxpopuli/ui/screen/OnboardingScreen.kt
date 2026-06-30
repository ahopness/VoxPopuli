package dev.lucasangelo.voxpopuli.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.ui.component.PagerScaffold
import dev.lucasangelo.voxpopuli.ui.component.PagerScaffoldContent
import dev.lucasangelo.voxpopuli.util.sourceCategoryMetas
import dev.lucasangelo.voxpopuli.viewmodel.OnboardingViewModel
import io.github.kdroidfilter.composemediaplayer.AudioMode
import io.github.kdroidfilter.composemediaplayer.InterruptionMode
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Composable
fun OnboardingScreen(
    rootNavController: NavController,
    rootShowMessage: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    PagerScaffold(
        title = "",
        canGoBack = !settings.showOnboarding,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 4,
        prelude = {
            val context = LocalContext.current
            val videoUri = "android.resource://${context.packageName}/${R.raw.background}"

            val playerState = rememberVideoPlayerState( audioMode = AudioMode(
                interruptionMode = InterruptionMode.MixWithOthers
            ) )
            LaunchedEffect(Unit) {
                playerState.volume = 0f
                playerState.loop = true
                playerState.openUri(videoUri)
            }

            VideoPlayerSurface(
                playerState = playerState,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
            )
        }
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf(
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        text = stringResource(R.string.onboarding_welcome),
                        modifier = Modifier.offset(y = (50).dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(250.dp)
                    )
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.app_name))
                            }
                            append(' ')
                            append(stringResource(R.string.onboarding_desc_suffix))
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.onboarding_description))
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(stringResource(R.string.onboarding_algorithm))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    ) {
                        sourceCategoryMetas.entries.forEach { entry ->
                            val category = entry.key
                            val isSubscribed = !profile.ignoredCategories.contains(category)

                            Button(
                                onClick = {
                                    val newIgnoredCategories = if (isSubscribed) {
                                        profile.ignoredCategories + category
                                    } else {
                                        profile.ignoredCategories - category
                                    }
                                    viewModel.updateProfileEmbeddings( updatedProfile =
                                        profile.copy(ignoredCategories = newIgnoredCategories)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) Color.White else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = stringResource(entry.value.second),
                                    color = if (isSubscribed) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(stringResource(R.string.onboarding_setup_done))

                    Button(onClick = {
                        if (settings.showOnboarding) {
                            viewModel.updateSettings(settings.copy(showOnboarding = false))

                            rootNavController.navigate(HomeRoute) {
                                popUpTo(OnboardingRoute) { inclusive = true }
                            }
                        } else {
                            rootNavController.popBackStack()
                        }
                    }) {
                        Text(stringResource(R.string.onboarding_enter_app))
                    }
                }
            }
        )
    }
}