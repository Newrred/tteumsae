package com.tteumsae.app.ui.saved

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.recommendation.matchesGangwonRegion
import com.tteumsae.app.domain.saved.SavedPlace
import com.tteumsae.app.ui.BottomNavigation
import com.tteumsae.app.ui.navigation.MainTab
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import kotlinx.coroutines.launch

private enum class SavedSort {
    DEFAULT,
    NAME,
}

private data class PlaceCardItem(
    val place: PlaceCandidate,
    val savedEntry: SavedPlace?,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SavedPlacesScreen(
    catalogPlaces: List<PlaceCandidate>,
    savedPlaces: List<SavedPlace>,
    selectedRegion: String,
    regions: List<String>,
    onRegionSelected: (String) -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleSave: (PlaceCandidate) -> Unit,
    onRestore: (SavedPlace) -> Unit,
    onOpenMap: (PlaceCandidate) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible
    var selectedCategory by rememberSaveable { mutableStateOf<PlaceCategory?>(null) }
    var regionMenuExpanded by remember { mutableStateOf(false) }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(SavedSort.DEFAULT) }
    var selectedPlace by remember { mutableStateOf<PlaceCandidate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    BackHandler(enabled = isImeVisible && selectedPlace == null) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    BackHandler(enabled = selectedPlace != null) { selectedPlace = null }

    selectedPlace?.let { place ->
        SavedPlaceDetailScreen(
            place = place,
            onBack = { selectedPlace = null },
            onOpenMap = { onOpenMap(place) },
        )
        return
    }

    val savedById = savedPlaces.associateBy { it.place.id }
    val catalogIds = catalogPlaces.mapTo(mutableSetOf()) { it.id }
    val allPlaces = (catalogPlaces + savedPlaces.map { it.place }).distinctBy { it.id }
    val visiblePlaces = allPlaces
        .asSequence()
        .filter { it.id in catalogIds || matchesGangwonRegion(it.address, selectedRegion) }
        .filter { !savedOnly || it.id in savedById }
        .filter { selectedCategory == null || it.category == selectedCategory }
        .filter { place ->
            query.isBlank() ||
                place.name.contains(query, ignoreCase = true) ||
                place.address.contains(query, ignoreCase = true)
        }
        .map { catalogPlace ->
            val savedEntry = savedById[catalogPlace.id]
            PlaceCardItem(
                place = if (savedEntry == null) {
                    catalogPlace
                } else {
                    catalogPlace.copy(tags = savedEntry.place.tags)
                },
                savedEntry = savedEntry,
            )
        }
        .let { entries ->
            when (sort) {
                SavedSort.DEFAULT -> entries
                SavedSort.NAME -> entries.sortedBy { it.place.name }
            }
        }
        .toList()
    val shouldLoadMore by remember(visiblePlaces.size, hasMore, isLoadingMore) {
        derivedStateOf {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            hasMore && !isLoadingMore &&
                (visiblePlaces.isEmpty() || lastVisibleIndex >= visiblePlaces.lastIndex - 6)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isImeVisible) {
                BottomNavigation(
                    selectedTab = MainTab.SAVED,
                    onTabSelected = onTabSelected,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 16.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("탐색 위치 검색", color = TteumMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .clickable { regionMenuExpanded = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(selectedRegion, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "지역 선택",
                            tint = TteumMuted,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = regionMenuExpanded,
                        onDismissRequest = { regionMenuExpanded = false },
                    ) {
                        regions.forEach { region ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        region,
                                        fontWeight = if (selectedRegion == region) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                    )
                                },
                                onClick = {
                                    onRegionSelected(region)
                                    regionMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        SavedFilterChip(
                            text = "전체",
                            selected = selectedCategory == null && !savedOnly,
                            onClick = {
                                selectedCategory = null
                                savedOnly = false
                            },
                        )
                    }
                    item {
                        SavedFilterChip(
                            text = "찜",
                            selected = savedOnly,
                            showHeart = true,
                            onClick = { savedOnly = !savedOnly },
                        )
                    }
                    items(PlaceCategory.entries) { category ->
                        SavedFilterChip(
                            text = when (category) {
                                PlaceCategory.CULTURE -> "문화/예술"
                                PlaceCategory.FESTIVAL -> "행사/공연/축제"
                                else -> category.label
                            },
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "스팟 (${visiblePlaces.size})",
                        color = TteumMuted,
                        fontWeight = FontWeight.Bold,
                    )
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text("아래로 스크롤하면 장소를 계속 불러와요.")
                            }
                        },
                        state = rememberTooltipState(),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "목록 안내",
                                tint = TteumMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                TextButton(
                    onClick = {
                        sort = if (sort == SavedSort.DEFAULT) SavedSort.NAME else SavedSort.DEFAULT
                    },
                ) {
                    Text(
                        if (sort == SavedSort.DEFAULT) "기본순⌄" else "이름순⌄",
                        color = TteumMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TteumRed)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(errorMessage, color = TteumMuted)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onRetry) { Text("다시 불러오기") }
                }
            } else if (visiblePlaces.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (catalogPlaces.isEmpty()) "등록된 장소가 아직 없어요." else "조건에 맞는 장소가 없어요.",
                        color = TteumMuted,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    gridItems(visiblePlaces, key = { it.place.id }) { place ->
                        SavedPlaceCard(
                            place = place.place,
                            isSaved = place.savedEntry != null,
                            onClick = { selectedPlace = place.place },
                            onToggleSave = {
                                val removedEntry = place.savedEntry
                                onToggleSave(place.place)
                                if (removedEntry != null) {
                                    coroutineScope.launch {
                                        if (
                                            snackbarHostState.showSnackbar(
                                                message = "저장을 해제했어요.",
                                                actionLabel = "되돌리기",
                                            ) == SnackbarResult.ActionPerformed
                                        ) {
                                            onRestore(removedEntry)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
