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
import dev.lucasangelo.voxpopuli.data.room.sourceCategoryInfo
import dev.lucasangelo.voxpopuli.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.voxpopuli.ui.component.FloatingNavigationBar
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedBookmarksScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedCategoryScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedCuratedScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedNewScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedSourceScreen
import dev.lucasangelo.voxpopuli.ui.screen.home.SettingsScreen
import dev.lucasangelo.voxpopuli.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
        if(settings == null) return

        val sources by viewModel.sources.collectAsStateWithLifecycle()

        val standardTabList = remember {
            listOf(
                HomeTabItem(
                    id = "settings",
                    icon = R.drawable.icon_settings,
                    title = R.string.feed_settings,
                    destination = { SettingsScreen() }
                ),
                HomeTabItem(
                    id = "bookmarks",
                    icon = R.drawable.icon_bookmark,
                    title = R.string.feed_bookmarks,
                    destination = { FeedBookmarksScreen() }
                ),
                HomeTabItem(
                    id = "curated",
                    icon = R.drawable.icon_home,
                    title = R.string.feed_curated,
                    destination = { FeedCuratedScreen() }
                ),
                HomeTabItem(
                    id = "new",
                    icon = R.drawable.icon_star,
                    title = R.string.feed_new,
                    destination = { FeedNewScreen() }
                ),
            )
        }
        val categoryTabList = remember {
            sourceCategoryInfo.map { HomeTabItem(
                id= "category_${it.key.name}",
                icon = it.value.first,
                title = it.value.second,
                destination = { FeedCategoryScreen(
                        customCategory = it.key
                ) }
            ) }
        }
        val sourceTabList = remember(sources) {
            sources.map { HomeTabItem(
                id = "source_${it.id}",
                icon = it.logoUrl,
                title = it.name,
                destination = { FeedSourceScreen(
                        customSource = it,
                ) }
            ) }
        }

        val tabList = remember(settings, sourceTabList) {
            when (settings!!.tabSelection) {
                TabSelection.CATEGORIES -> standardTabList + categoryTabList
                TabSelection.SOURCES -> standardTabList + sourceTabList
            }
        }

        val pagerState = rememberPagerState(
            initialPage = 2,
            pageCount = { tabList.size }
        )
        HorizontalPager(
            pagerState,
            key = { tabList[it].id },
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
    val id: String,
    val icon: Any,
    val title: Any,
    val destination: @Composable () -> Unit
)