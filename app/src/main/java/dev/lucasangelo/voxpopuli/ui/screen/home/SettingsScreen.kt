package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import dev.lucasangelo.voxpopuli.data.datastore.TabSelection
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.sourceCategoryInfo
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.voxpopuli.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.voxpopuli.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.voxpopuli.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    if (settings == null) return
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    if (profile == null) return

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    CleanScaffold(
        topBar = { FloatingExtendedTopBar(
            listState = listState,
            canGoBack = false,
            title = stringResource(R.string.feed_settings),
            iconContent = { modifier, _ ->
                Icon(
                    painter = painterResource(R.drawable.icon_settings),
                    contentDescription = null,
                    modifier = modifier.clickable(onClick = {
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                    })
                )
            },
            actions = emptyList()
        ) }
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding + 16.dp)) }

            item { Text(stringResource(R.string.actions)) }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RetuneRecommendations(
                        profile!!,
                        onProfileEmbeddingsUpdated = { viewModel.updateProfileEmbedding(it) }
                    )
                    ChangeTabSelection(
                        settings!!,
                        onSettingsUpdated = { viewModel.updateSettings(it) }
                    )
                }
            }

            item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetuneRecommendations(
    profile: Profile,
    onProfileEmbeddingsUpdated: (Profile) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    Button(
        onClick = { showOptionsModal = true }
    ) {
        Text(stringResource(R.string.settings_retune))
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            showOptionsModal = false
        }
    }
    if (showOptionsModal)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            containerColor = Color.Black
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_retune_choose),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                val newIgnoredCategories = remember { SourceCategory.entries.toMutableStateList() }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                ) {
                    sourceCategoryInfo.entries.forEach { entry ->
                        val category = entry.key
                        val isSubscribed = !newIgnoredCategories.contains(category)

                        Button(
                            onClick = {
                                if (isSubscribed)
                                    newIgnoredCategories.add(category)
                                else
                                    newIgnoredCategories.remove(category)
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

                Button(onClick = {
                    onProfileEmbeddingsUpdated(
                        profile.copy(ignoredCategories = newIgnoredCategories)
                    )

                    onDismissRequest()
                } ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeTabSelection(
    settings: Settings,
    onSettingsUpdated: (Settings) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    Button(
        onClick = { showOptionsModal = true }
    ) {
        Text(stringResource(R.string.settings_tabs))
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            showOptionsModal = false
        }
    }
    if (showOptionsModal)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            containerColor = Color.Black
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_tabs_choose),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        onSettingsUpdated(settings.copy(tabSelection = TabSelection.CATEGORIES))
                        onDismissRequest()
                    }) {
                        Text(stringResource(R.string.settings_tabs_categories))
                    }
                    Button(onClick = {
                        onSettingsUpdated(settings.copy(tabSelection = TabSelection.SOURCES))
                        onDismissRequest()
                    }) {
                        Text(stringResource(R.string.settings_tabs_sources))
                    }
                }
            }
        }
}