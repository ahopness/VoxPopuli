package dev.lucasangelo.voxpopuli.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.datastore.TabSelection
import dev.lucasangelo.voxpopuli.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.voxpopuli.ui.component.FloatingNavigationBar
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedType
import dev.lucasangelo.voxpopuli.ui.screen.home.SettingsScreen
import dev.lucasangelo.voxpopuli.util.sourceCategoryMetas
import dev.lucasangelo.voxpopuli.util.feedTypeMetas
import dev.lucasangelo.voxpopuli.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.collections.listOf

@Serializable
object HomeRoute

@Composable
fun HomeScreen(
    rootNavController: NavController,
    rootShowMessage: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    Box(Modifier.fillMaxSize()) {
        val settings by viewModel.settings.collectAsStateWithLifecycle()

        val sourcesList by viewModel.sources.collectAsStateWithLifecycle()
        val sourcesMap = remember(sourcesList) { sourcesList.associateBy { it.id } }

        val settingsTab = remember {
            HomeTabItem(
                icon = R.drawable.icon_settings,
                title = R.string.feed_settings,
                destination = { SettingsScreen() }
            )
        }
        val standardTabList = remember(sourcesMap) {
            feedTypeMetas.map { HomeTabItem(
                    icon = it.value.first,
                    title = it.value.second,
                    destination = { FeedScreen(
                        type = it.key,
                        sourcesMap,
                    ) }
            ) }
        }
        val categoryTabList = remember(sourcesMap) {
            sourceCategoryMetas.map { HomeTabItem(
                    icon = it.value.first,
                    title = it.value.second,
                    destination = { FeedScreen(
                        type = FeedType.CATEGORY,
                        sourcesMap,
                        customCategory = it.key
                    ) }
            ) }
        }
        val sourceTabList = remember(sourcesList) {
            sourcesList.map { HomeTabItem(
                icon = it.logoUrl,
                title = it.name,
                destination = { FeedScreen(
                    type = FeedType.SOURCE,
                    sourcesMap,
                    customSource = it,
                ) }
            ) }
        }
        // TODO: 'add new source' button

        val tabList = listOf(settingsTab) +
            when (settings.tabSelection) {
                TabSelection.CATEGORIES -> standardTabList + categoryTabList
                TabSelection.SOURCES -> standardTabList + sourceTabList
            }

        val pagerState = rememberPagerState(
            initialPage = 3,
            pageCount = { tabList.size }
        )
        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            tabList[page].destination()
        }

        val coroutineScope = rememberCoroutineScope()
        FloatingNavigationBar(
            selectedIndex = pagerState.currentPage,
            items = tabList.mapIndexed { index, item ->
                FloatingNavigationActionItem(
                    item.icon,
                    item.title,
                    showTitle = pagerState.currentPage == index,
                    action = { coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    } }
                )
            }
        )
    }
}
data class HomeTabItem(
    val icon: Any,
    val title: Any,
    val destination: @Composable () -> Unit
)