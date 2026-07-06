package dev.lucasangelo.voxpopuli.ui.screen.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import dev.lucasangelo.voxpopuli.data.datastore.TabSelection
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.data.room.sourceCategoryInfo
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.voxpopuli.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.voxpopuli.ui.component.MonochromeAsyncImage
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

    val sources by viewModel.sources.collectAsStateWithLifecycle()

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

            item {
                SettingsChangeTabSelection(
                    settings!!,
                    onSettingsUpdated = { viewModel.updateSettings(it) }
                )
            }

            item {
                SettingsRetuneRecommendations(
                    profile!!,
                    onProfileEmbeddingsUpdated = { viewModel.updateProfileEmbedding(it) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { Text(stringResource(R.string.settings_sources_title)) }

            items(sources, key = { it.id }) { source ->
                SettingsSource(
                    source,
                    onSourceUpdateRequest = {
                        viewModel.updateSource(it)
                    },
                    onSourceDeleteRequest = {
                        viewModel.deleteSource(it)
                    }
                )
            }

            item {
                SettingsNewSourceButton(
                    onSourceInsertRequest = {
                        viewModel.insertSource(it)
                    }
                )
            }

            item { Spacer(Modifier.height(64.dp)) }
            item {
                val context = LocalContext.current
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(Intent(
                            Intent.ACTION_VIEW,
                            "https://lucasangelo.dev/links/".toUri()
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Any questions? Get in touch.")

                        Icon(
                            painter = painterResource(R.drawable.icon_help),
                            contentDescription = null,
                            modifier = Modifier.size(smallIconSize)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
        }
    }
}

val iconSize = 48.dp
val smallIconSize = 32.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsChangeTabSelection(
    settings: Settings,
    onSettingsUpdated: (Settings) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showOptionsModal = true },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.settings_tabs))
                    append(' ')
                    if (settings.tabSelection == TabSelection.CATEGORIES)
                        append(stringResource(R.string.settings_tabs_categories))
                    else if (settings.tabSelection == TabSelection.SOURCES)
                        append(stringResource(R.string.settings_tabs_sources))
                }
            )

            Icon(
                painter = painterResource(R.drawable.icon_expand),
                contentDescription = null,
                modifier = Modifier.size(smallIconSize)
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_tabs_choose),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSettingsUpdated(settings.copy(tabSelection = TabSelection.CATEGORIES))
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.settings_tabs_categories))
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSettingsUpdated(settings.copy(tabSelection = TabSelection.SOURCES))
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.settings_tabs_sources))
                    }
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRetuneRecommendations(
    profile: Profile,
    onProfileEmbeddingsUpdated: (Profile) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showOptionsModal = true },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_retune))

            Icon(
                painter = painterResource(R.drawable.icon_repost),
                contentDescription = null,
                modifier = Modifier.size(smallIconSize)
            )
        }
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

                        OutlinedButton(
                            onClick = {
                                if (isSubscribed)
                                    newIgnoredCategories.add(category)
                                else
                                    newIgnoredCategories.remove(category)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSubscribed) Color.White else Color.Transparent
                            ),
                        ) {
                            Text(
                                text = stringResource(entry.value.second),
                                color = if (isSubscribed) Color.Black else Color.White
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onProfileEmbeddingsUpdated(
                            profile.copy(ignoredCategories = newIgnoredCategories)
                        )

                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSource(
    source: SourceEntity,
    onSourceUpdateRequest: (SourceEntity) -> Unit,
    onSourceDeleteRequest: (SourceEntity) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(width = 1.dp, color = Color.DarkGray),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 8.dp)
            .padding(start = 24.dp, end = 16.dp)
            .clickable(onClick = { showOptionsModal = true }),
    ) {
        MonochromeAsyncImage(
            model = source.logoUrl,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )

        Text(
            text = source.name,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.icon_edit),
            contentDescription = stringResource(R.string.settings_sources_edit_button),
            modifier = Modifier.size(iconSize)
        )
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
        SettingsEditSourceModal(
            sheetState,
            onDismissRequest,
            source,
            onSourceUpdateRequest = onSourceUpdateRequest,
            onSourceDeleteRequest = onSourceDeleteRequest,
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNewSourceButton(
    onSourceInsertRequest: (SourceEntity) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showOptionsModal = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_sources_new))

            Icon(
                painter = painterResource(R.drawable.icon_add),
                contentDescription = null,
                modifier = Modifier.size(smallIconSize)
            )
        }
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
        SettingsEditSourceModal(
            sheetState,
            onDismissRequest,
            source = null,
            onSourceInsertRequest = onSourceInsertRequest,
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsEditSourceModal(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    source: SourceEntity?,
    onSourceInsertRequest: (SourceEntity) -> Unit = {},
    onSourceUpdateRequest: (SourceEntity) -> Unit = {},
    onSourceDeleteRequest: (SourceEntity) -> Unit = {},
) {
    var sourceName by remember { mutableStateOf(source?.name ?: "") }
    var sourceCategory by remember { mutableStateOf(source?.category ?: SourceCategory.GENERAL) }
    var sourceLogoUrl by remember { mutableStateOf(source?.logoUrl ?: "") }
    var sourceFeedUrl by remember { mutableStateOf(source?.feedUrl ?: "") }

    var sourceNameIsError by remember { mutableStateOf(false) }
    var sourceLogoUrlIsError by remember { mutableStateOf(false) }
    var sourceFeedUrlIsError by remember { mutableStateOf(false) }
    fun checkForErrors() : Boolean {
        sourceNameIsError = (sourceName.trim().isEmpty())
        sourceLogoUrlIsError = (sourceLogoUrl.trim().isEmpty())
        sourceFeedUrlIsError = (sourceFeedUrl.trim().isEmpty())

        if (sourceNameIsError || sourceLogoUrlIsError || sourceFeedUrlIsError)
            return true
        else
            return false
    }

    var showDeletionRequest by remember { mutableStateOf(false) }

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
                text =
                    if (source == null)
                        stringResource(R.string.settings_sources_title_create)
                    else
                        stringResource(R.string.settings_sources_title_edit),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sourceName,
                onValueChange = { sourceName = it },
                label = { Text(stringResource(R.string.settings_sources_edit_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                isError = sourceNameIsError,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sourceLogoUrl,
                onValueChange = { sourceLogoUrl = it },
                label = { Text(stringResource(R.string.settings_sources_edit_logourl)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray),
                isError = sourceLogoUrlIsError,
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSelectCategoryButton(
                sourceCategory,
                onCategoryChanged = { sourceCategory = it }
            )

            Icon(
                painter = painterResource(R.drawable.divider_horizontal),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            OutlinedTextField(
                value = sourceFeedUrl,
                onValueChange = { sourceFeedUrl = it },
                label = { Text(stringResource(R.string.settings_sources_edit_feedurl)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray),
                isError = sourceFeedUrlIsError,
                modifier = Modifier.fillMaxWidth()
            )

            if (source == null)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!checkForErrors()) {
                            onSourceInsertRequest(
                                SourceEntity(
                                    name = sourceName.trim(),
                                    logoUrl = sourceLogoUrl.trim(),
                                    category = sourceCategory,
                                    feedUrl = sourceFeedUrl.trim()
                                )
                            )
                            onDismissRequest()
                        }
                    }
                ) {
                    Text(stringResource(R.string.settings_sources_button_create))
                }
            else
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.Red.copy(0.75f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(0.75f)),
                        onClick = {
                            showDeletionRequest = true
                        },
                    ) {
                        Text(stringResource(R.string.settings_sources_button_delete))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!checkForErrors()) {
                                onSourceUpdateRequest(
                                    source.copy(
                                        name = sourceName.trim(),
                                        logoUrl = sourceLogoUrl.trim(),
                                        category = sourceCategory,
                                        feedUrl = sourceFeedUrl.trim()
                                    )
                                )
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.settings_sources_button_edit))
                    }
                }
        }
    }

    if (showDeletionRequest)
        DeleteConfirmationDialog(
            text = stringResource(R.string.delete_source_warning),
            onDismiss = { showDeletionRequest = false },
            onConfirm = {
                onSourceDeleteRequest(source!!)
                onDismissRequest()
            }
        )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSelectCategoryButton(
    category: SourceCategory,
    onCategoryChanged: (SourceCategory) -> Unit,
) {
    var showOptionsModal by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showOptionsModal = true },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(sourceCategoryInfo[category]!!.first),
                    contentDescription = null,
                    modifier = Modifier.size(smallIconSize)
                )
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.settings_sources_edit_category))
                        append(' ')
                        append(stringResource(sourceCategoryInfo[category]!!.second))
                    }
                )
            }

            Icon(
                painter = painterResource(R.drawable.icon_expand),
                contentDescription = null,
                modifier = Modifier.size(smallIconSize)
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sourceCategoryInfo.forEach { (category, pair) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .clickable(onClick = {
                                onCategoryChanged(category)
                                onDismissRequest()
                            })
                    ) {
                        Icon(
                            painter = painterResource(pair.first),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        Text(
                            text = stringResource(pair.second),
                        )
                    }
                }
            }
        }
}