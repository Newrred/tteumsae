package com.tteumsae.app.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.LruCache
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.tteumsae.app.BuildConfig
import com.tteumsae.app.data.TteumsaeApi
import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.LocationSearchResult
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SafetyLevel
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.domain.SearchMode
import com.tteumsae.app.domain.TransportMode
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt

private enum class AppScreen {
    HOME,
    SAVED,
    SETTINGS,
    LOCATION,
    TIME,
    CONDITIONS,
    LOADING,
    RESULTS,
    DETAIL,
}

private enum class MainTab {
    EXPLORE,
    SAVED,
    SETTINGS,
}

private enum class SavedSort {
    RECENT,
    NAME,
}

private const val MIN_DEADLINE_MINUTES = 15
private const val MAX_DEADLINE_MINUTES = 360

private data class SavedPlaceEntry(
    val place: PlaceCandidate,
    val savedAtMillis: Long,
)

private data class PlaceCardItem(
    val place: PlaceCandidate,
    val savedEntry: SavedPlaceEntry?,
)

private val savedImageCache = object : LruCache<String, Bitmap>(16 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
}

@Composable
fun TteumsaeApp() {
    val context = LocalContext.current
    val api = remember { TteumsaeApi() }
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var mode by rememberSaveable { mutableStateOf(SearchMode.ON_THE_WAY) }
    var startName by rememberSaveable { mutableStateOf("강릉역") }
    var endName by rememberSaveable { mutableStateOf("강릉 안목해변") }
    var startLocation by remember { mutableStateOf<LocationSearchResult?>(null) }
    var endLocation by remember { mutableStateOf<LocationSearchResult?>(null) }
    var deadline by rememberSaveable { mutableStateOf(90) }
    var buffer by rememberSaveable { mutableStateOf(15) }
    var transport by rememberSaveable { mutableStateOf(TransportMode.CAR) }
    var categories by remember { mutableStateOf(emptySet<PlaceCategory>()) }
    var excludeRestaurants by rememberSaveable { mutableStateOf(false) }
    var recommendations by remember { mutableStateOf(emptyList<SafeRecommendation>()) }
    var selected by remember { mutableStateOf<SafeRecommendation?>(null) }
    var activeCriteria by remember { mutableStateOf<SearchCriteria?>(null) }
    var locationChecking by remember { mutableStateOf(false) }
    var savedPlaces by remember { mutableStateOf(loadSavedPlaces(context)) }
    var catalogPlaces by remember { mutableStateOf(emptyList<PlaceCandidate>()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogLoadingMore by remember { mutableStateOf(false) }
    var catalogPage by remember { mutableStateOf(1) }
    var catalogHasMore by remember { mutableStateOf(true) }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var catalogLoadAttempt by rememberSaveable { mutableStateOf(0) }
    val appScope = rememberCoroutineScope()
    val updateSavedPlaces: (List<SavedPlaceEntry>) -> Unit = { updated ->
        savedPlaces = updated
        storeSavedPlaces(context, updated)
    }

    LaunchedEffect(screen, catalogLoadAttempt) {
        if (screen == AppScreen.SAVED) {
            catalogLoading = true
            catalogError = null
            try {
                val firstPage = api.places()
                catalogPlaces = firstPage.places
                catalogPage = 1
                catalogHasMore = firstPage.hasMore
            } catch (error: Exception) {
                catalogError = error.message ?: "TourAPI 장소를 불러오지 못했어요."
            } finally {
                catalogLoading = false
            }
        }
    }

    val criteria = SearchCriteria(
        mode = mode,
        startName = startName.ifBlank { "강릉역" },
        endName = endName.ifBlank { if (mode == SearchMode.NEARBY) startName else "강릉 안목해변" },
        deadlineMinutesFromNow = deadline,
        safetyBufferMinutes = buffer,
        transportMode = if (mode == SearchMode.ON_THE_WAY) TransportMode.CAR else transport,
        categories = recommendationCategories(categories, excludeRestaurants),
        startCoordinates = startLocation?.coordinates,
        endCoordinates = endLocation?.coordinates,
    )

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            onStart = { coordinates ->
                if (coordinates != null) {
                    startName = "현재 위치"
                    startLocation = LocationSearchResult(
                        id = "current-location",
                        name = "현재 위치",
                        address = "GPS로 확인한 위치",
                        coordinates = coordinates,
                    )
                }
                screen = AppScreen.LOCATION
            },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppScreen.HOME
                    MainTab.SAVED -> AppScreen.SAVED
                    MainTab.SETTINGS -> AppScreen.SETTINGS
                }
            },
        )

        AppScreen.SAVED -> SavedPlacesScreen(
            catalogPlaces = catalogPlaces,
            savedPlaces = savedPlaces,
            isLoading = catalogLoading,
            isLoadingMore = catalogLoadingMore,
            hasMore = catalogHasMore,
            errorMessage = catalogError,
            onRetry = { catalogLoadAttempt += 1 },
            onLoadMore = {
                if (!catalogLoading && !catalogLoadingMore && catalogHasMore) {
                    catalogLoadingMore = true
                    appScope.launch {
                        try {
                            val nextPage = api.places(page = catalogPage + 1)
                            catalogPlaces = (catalogPlaces + nextPage.places)
                                .distinctBy { it.id }
                            catalogPage += 1
                            catalogHasMore = nextPage.hasMore
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                "장소를 더 불러오지 못했어요.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } finally {
                            catalogLoadingMore = false
                        }
                    }
                }
            },
            onToggleSave = { place ->
                val existing = savedPlaces.find { it.place.id == place.id }
                if (existing == null) {
                    updateSavedPlaces(listOf(SavedPlaceEntry(place, System.currentTimeMillis())) + savedPlaces)
                } else {
                    updateSavedPlaces(savedPlaces.filterNot { it.place.id == place.id })
                }
            },
            onRestore = { entry ->
                updateSavedPlaces(
                    (savedPlaces.filterNot { it.place.id == entry.place.id } + entry)
                        .sortedByDescending { it.savedAtMillis },
                )
            },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppScreen.HOME
                    MainTab.SAVED -> AppScreen.SAVED
                    MainTab.SETTINGS -> AppScreen.SETTINGS
                }
            },
        )

        AppScreen.SETTINGS -> SettingsTabScreen(
            savedCount = savedPlaces.size,
            onClearSaved = { updateSavedPlaces(emptyList()) },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppScreen.HOME
                    MainTab.SAVED -> AppScreen.SAVED
                    MainTab.SETTINGS -> AppScreen.SETTINGS
                }
            },
        )

        AppScreen.LOCATION -> LocationScreen(
            mode = mode,
            startName = startName,
            endName = endName,
            startLocation = startLocation,
            endLocation = endLocation,
            searchPlaces = api::searchPlaces,
            onModeChange = {
                mode = it
                if (it == SearchMode.NEARBY) {
                    endName = startName
                    endLocation = startLocation
                } else {
                    endName = "강릉 안목해변"
                    endLocation = null
                }
                if (it == SearchMode.ON_THE_WAY) transport = TransportMode.CAR
            },
            onStartNameChange = {
                startName = it
                startLocation = null
            },
            onEndNameChange = {
                endName = it
                endLocation = null
            },
            onStartSelected = {
                startName = it.name
                startLocation = it
            },
            onEndSelected = {
                endName = it.name
                endLocation = it
            },
            onUseStartAsEnd = {
                endName = startName
                endLocation = startLocation
            },
            isChecking = locationChecking,
            onBack = { screen = AppScreen.HOME },
            onNext = {
                if (!locationChecking) {
                    locationChecking = true
                    appScope.launch {
                        try {
                            val resolvedStart = startLocation ?: api.searchPlace(startName)
                            val resolvedEnd = if (mode == SearchMode.NEARBY) {
                                resolvedStart
                            } else {
                                endLocation ?: api.searchPlace(endName)
                            }
                            if (mode == SearchMode.NEARBY && !api.isGangwon(resolvedStart.coordinates)) {
                                Toast.makeText(
                                    context,
                                    "현재 위치는 강원도 밖이라 근처 장소를 추천할 수 없어요.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                startName = resolvedStart.name
                                startLocation = resolvedStart
                                endName = resolvedEnd.name
                                endLocation = resolvedEnd
                                screen = AppScreen.TIME
                            }
                        } catch (error: Exception) {
                            Toast.makeText(
                                context,
                                error.message ?: "위치를 확인하지 못했어요.",
                                Toast.LENGTH_LONG,
                            ).show()
                        } finally {
                            locationChecking = false
                        }
                    }
                }
            },
        )

        AppScreen.TIME -> TimeScreen(
            mode = mode,
            deadline = deadline,
            buffer = buffer,
            transport = transport,
            onDeadlineChange = { deadline = it },
            onBufferChange = { buffer = it },
            onTransportChange = { transport = it },
            onBack = { screen = AppScreen.LOCATION },
            onNext = { screen = AppScreen.CONDITIONS },
        )

        AppScreen.CONDITIONS -> ConditionsScreen(
            mode = mode,
            categories = categories,
            excludeRestaurants = excludeRestaurants,
            onToggleCategory = { category ->
                categories = if (category in categories) {
                    categories - category
                } else {
                    categories + category
                }
            },
            onExcludeRestaurantsChange = { exclude ->
                excludeRestaurants = exclude
                if (exclude) categories = categories - PlaceCategory.RESTAURANT
            },
            onClear = {
                categories = emptySet()
                excludeRestaurants = false
            },
            onBack = { screen = AppScreen.TIME },
            onSearch = { screen = AppScreen.LOADING },
        )

        AppScreen.LOADING -> LoadingScreen(
            onLoad = {
                val start = startLocation ?: api.searchPlace(criteria.startName)
                val end = if (criteria.mode == SearchMode.NEARBY) {
                    start
                } else {
                    endLocation ?: api.searchPlace(criteria.endName)
                }
                val resolvedCriteria = criteria.copy(
                    startName = start.name,
                    endName = end.name,
                    startCoordinates = start.coordinates,
                    endCoordinates = end.coordinates,
                )
                recommendations = api.recommendations(resolvedCriteria)
                activeCriteria = resolvedCriteria
            },
            onFinished = { screen = AppScreen.RESULTS },
            onBack = { screen = AppScreen.CONDITIONS },
        )

        AppScreen.RESULTS -> ResultsScreen(
            criteria = activeCriteria ?: criteria,
            recommendations = recommendations,
            onBack = { screen = AppScreen.CONDITIONS },
            onEdit = { screen = AppScreen.TIME },
            onSelect = {
                selected = it
                screen = AppScreen.DETAIL
            },
        )

        AppScreen.DETAIL -> selected?.let {
            DetailScreen(
                criteria = activeCriteria ?: criteria,
                recommendation = it,
                isSaved = savedPlaces.any { entry -> entry.place.id == it.place.id },
                onToggleSave = {
                    updateSavedPlaces(if (savedPlaces.any { entry -> entry.place.id == it.place.id }) {
                        savedPlaces.filterNot { entry -> entry.place.id == it.place.id }
                    } else {
                        listOf(SavedPlaceEntry(it.place, System.currentTimeMillis())) + savedPlaces
                    })
                },
                onBack = { screen = AppScreen.RESULTS },
            )
        } ?: run {
            screen = AppScreen.RESULTS
        }
    }
}

@Composable
private fun HomeScreen(
    onStart: (Coordinates?) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val context = LocalContext.current
    var currentLocationTarget by remember { mutableStateOf<RequestedMapLocation?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var locationRequestId by remember { mutableStateOf(0L) }

    val locateCurrentPosition: () -> Unit = {
        isLocating = true
        requestCurrentLocation(
            context = context,
            onSuccess = { location ->
                locationRequestId += 1
                currentLocationTarget = RequestedMapLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    requestId = locationRequestId,
                )
                isLocating = false
            },
            onLocationDisabled = {
                isLocating = false
                showLocationSettingsDialog = true
            },
            onUnavailable = {
                isLocating = false
                Toast.makeText(
                    context,
                    "현재 위치를 확인하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                    Toast.LENGTH_SHORT,
                ).show()
            },
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { it }) {
            locateCurrentPosition()
        } else {
            Toast.makeText(
                context,
                "현재 위치를 사용하려면 위치 권한을 허용해 주세요.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                selectedTab = MainTab.EXPLORE,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            MapBackground(
                modifier = Modifier.fillMaxSize(),
                requestedLocation = currentLocationTarget,
            )
            SearchBar(
                text = "출발지 또는 현재 위치 검색",
                onClick = {
                    onStart(
                        currentLocationTarget?.let {
                            Coordinates(it.latitude, it.longitude)
                        },
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(20.dp),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 20.dp, top = 96.dp, end = 20.dp),
                color = Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 5.dp,
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "강원도에서 남는 시간, 어디 갈까?",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "이동 시간과 머무는 시간을 계산해 시간 안에 다녀올 장소를 찾아드려요.",
                        color = TteumMuted,
                        fontSize = 14.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RoundMapButton(
                    onClick = if (isLocating) null else {
                        {
                            if (currentLocationTarget != null) {
                                currentLocationTarget = null
                            } else if (hasLocationPermission(context)) {
                                locateCurrentPosition()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            }
                        }
                    },
                    foreground = if (currentLocationTarget != null) TteumRed else TteumInk,
                    border = if (currentLocationTarget != null) {
                        BorderStroke(1.5.dp, TteumRed)
                    } else {
                        null
                    },
                    icon = {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = TteumRed,
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "현재 위치",
                            )
                        }
                    },
                )
            }

            Button(
                onClick = {
                    onStart(
                        currentLocationTarget?.let {
                            Coordinates(it.latitude, it.longitude)
                        },
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("남는 시간으로 장소 찾기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("위치 서비스를 켜 주세요") },
            text = { Text("현재 위치를 찾으려면 휴대폰의 위치 서비스를 켜야 합니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationSettingsDialog = false
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    },
                ) {
                    Text("위치 설정 열기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun BottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = TteumRed.copy(alpha = 0.10f),
                spotColor = TteumRed.copy(alpha = 0.10f),
            ),
        color = Color.White,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 12.dp),
        ) {
            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "장소 찾기",
                selected = selectedTab == MainTab.EXPLORE,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(MainTab.EXPLORE) },
            )
            BottomNavItem(
                icon = Icons.Default.FormatListBulleted,
                label = "장소 둘러보기",
                selected = selectedTab == MainTab.SAVED,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(MainTab.SAVED) },
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "설정",
                selected = selectedTab == MainTab.SETTINGS,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(MainTab.SETTINGS) },
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val color = if (selected) TteumInk else Color(0xFFA0A4AC)
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedPlacesScreen(
    catalogPlaces: List<PlaceCandidate>,
    savedPlaces: List<SavedPlaceEntry>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleSave: (PlaceCandidate) -> Unit,
    onRestore: (SavedPlaceEntry) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    var selectedCategory by rememberSaveable { mutableStateOf<PlaceCategory?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(SavedSort.RECENT) }
    var selectedPlace by remember { mutableStateOf<PlaceCandidate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    selectedPlace?.let { place ->
        SavedPlaceDetailScreen(
            place = place,
            onBack = { selectedPlace = null },
        )
        return
    }

    val savedById = savedPlaces.associateBy { it.place.id }
    val visiblePlaces = catalogPlaces
        .asSequence()
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
                SavedSort.RECENT -> entries.sortedWith(
                    compareByDescending<PlaceCardItem> { it.savedEntry?.savedAtMillis ?: Long.MIN_VALUE }
                        .thenBy { it.place.name },
                )
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
            BottomNavigation(
                selectedTab = MainTab.SAVED,
                onTabSelected = onTabSelected,
            )
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
                    .padding(top = 20.dp, bottom = 16.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("장소 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("강원도 전체", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        SavedFilterChip(
                            text = "전체",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                        )
                    }
                    items(PlaceCategory.entries) { category ->
                        SavedFilterChip(
                            text = category.label,
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
                        "현재 ${visiblePlaces.size}개 표시 중",
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
                        sort = if (sort == SavedSort.RECENT) SavedSort.NAME else SavedSort.RECENT
                    },
                ) {
                    Text(
                        if (sort == SavedSort.RECENT) "저장 우선순⌄" else "이름순⌄",
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
                    modifier = Modifier.fillMaxSize(),
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

@Composable
private fun SettingsTabScreen(
    savedCount: Int,
    onClearSaved: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        bottomBar = {
            BottomNavigation(
                selectedTab = MainTab.SETTINGS,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text("설정", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("위치 권한", fontWeight = FontWeight.Bold)
                    Text(
                        if (hasLocationPermission(context)) "허용됨" else "허용되지 않음",
                        color = TteumMuted,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}"),
                                ),
                            )
                        },
                    ) {
                        Text("앱 권한 설정 열기")
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("저장한 장소", fontWeight = FontWeight.Bold)
                    Text("${savedCount}개가 이 기기에 저장되어 있어요.", color = TteumMuted)
                    if (savedCount > 0) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = { showClearDialog = true }) {
                            Text("저장 목록 비우기")
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("데이터 출처", fontWeight = FontWeight.Bold)
            Text("한국관광공사 TourAPI · 카카오맵", color = TteumMuted)
            Spacer(Modifier.height(16.dp))
            Text("앱 버전", fontWeight = FontWeight.Bold)
            Text(BuildConfig.VERSION_NAME, color = TteumMuted)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("저장 목록을 비울까요?") },
            text = { Text("이 기기에 저장한 장소가 모두 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearSaved()
                        showClearDialog = false
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SavedPlaceDetailScreen(
    place: PlaceCandidate,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { openKakaoMap(context, place.name) },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("카카오맵 실행 및 안내", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                Box {
                    SavedPlaceImage(
                        imageUrl = place.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(14.dp)
                            .background(Color.White.copy(alpha = 0.92f), CircleShape),
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            }
            item {
                Column(Modifier.padding(20.dp)) {
                    Text(place.category.label, color = TteumRed, fontWeight = FontWeight.Bold)
                    Text(place.name, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    if (place.address.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(place.address, color = TteumMuted)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "추천 머무는 시간 ${formatMinutes(place.stayMinutes)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(place.tags) { tag ->
                            Surface(
                                color = Color(0xFFF1F2F4),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    tag,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    color = Color(0xFF55585F),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) TteumInk else Color(0xFFF4F5F7),
        shape = RoundedCornerShape(50),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE1E3E8)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
            color = if (selected) Color.White else Color(0xFF596170),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SavedPlaceCard(
    place: PlaceCandidate,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
) {
    val (visibleTags, hiddenTagCount) = compactTags(place.tags)
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column {
            Box {
                SavedPlaceImage(
                    imageUrl = place.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                )
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape),
                ) {
                    Icon(
                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isSaved) "저장 해제" else "저장",
                        tint = if (isSaved) TteumRed else Color(0xFFB8BBC1),
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    place.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 155.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(place.category.label, color = Color(0xFF55585F), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "추천 머무는 시간 ${formatMinutes(place.stayMinutes)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                val compactItems = visibleTags + if (hiddenTagCount > 0) listOf("+ $hiddenTagCount") else emptyList()
                compactItems.chunked(2).forEach { rowTags ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowTags.forEach { tag ->
                            Surface(
                                color = Color(0xFFF1F2F4),
                                shape = RoundedCornerShape(3.dp),
                            ) {
                                Text(
                                    tag,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    color = Color(0xFF55585F),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun SavedPlaceImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null,
        imageUrl,
    ) {
        value = if (imageUrl.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                savedImageCache.get(imageUrl)?.asImageBitmap() ?: runCatching {
                    val connection = URL(imageUrl).openConnection().apply {
                        connectTimeout = 8_000
                        readTimeout = 8_000
                    }
                    connection.getInputStream().use { BitmapFactory.decodeStream(it) }
                        ?.also { savedImageCache.put(imageUrl, it) }
                        ?.asImageBitmap()
                }.getOrNull()
            }
        }
    }

    if (bitmap == null) {
        Box(modifier = modifier.background(Color(0xFFC4C4C4)))
    } else {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

internal fun compactTags(
    tags: List<String>,
    characterBudget: Int = 18,
): Pair<List<String>, Int> {
    var used = 0
    val visible = tags.takeWhile { tag ->
        val fits = used + tag.length + 1 <= characterBudget
        if (fits) used += tag.length + 1
        fits
    }
    return visible to (tags.size - visible.size)
}

internal fun recommendationCategories(
    selected: Set<PlaceCategory>,
    excludeRestaurants: Boolean,
): Set<PlaceCategory> {
    if (!excludeRestaurants) return selected
    return (selected.ifEmpty { PlaceCategory.entries.toSet() } - PlaceCategory.RESTAURANT)
        .ifEmpty { PlaceCategory.entries.toSet() - PlaceCategory.RESTAURANT }
}

private fun formatMinutes(minutes: Int): String =
    when {
        minutes < 60 -> "${minutes}분"
        minutes % 60 == 0 -> "${minutes / 60}시간"
        else -> "${minutes / 60}시간 ${minutes % 60}분"
    }

private fun PlaceCategory.activityLabel(): String = when (this) {
    PlaceCategory.ATTRACTION -> "관광·구경"
    PlaceCategory.RESTAURANT -> "식사"
    PlaceCategory.CAFE -> "카페·휴식"
    PlaceCategory.CULTURE -> "전시·문화"
    PlaceCategory.FESTIVAL -> "축제·행사"
    PlaceCategory.SHOPPING -> "쇼핑"
    PlaceCategory.LEISURE -> "체험·레포츠"
}

private fun recommendationConditionSummary(categories: Set<PlaceCategory>): String? = when {
    categories.isEmpty() -> null
    categories.size == PlaceCategory.entries.size - 1 &&
        PlaceCategory.RESTAURANT !in categories -> "음식점 제외"
    else -> categories.joinToString(" · ") { it.activityLabel() }
}

private const val SAVED_PLACES_PREFERENCES = "saved_places"
private const val SAVED_PLACES_KEY = "entries"

private fun loadSavedPlaces(context: Context): List<SavedPlaceEntry> {
    val json = context
        .getSharedPreferences(SAVED_PLACES_PREFERENCES, Context.MODE_PRIVATE)
        .getString(SAVED_PLACES_KEY, null)
        ?: return emptyList()

    return runCatching {
        val entries = JSONArray(json)
        buildList {
            for (index in 0 until entries.length()) {
                val item = entries.getJSONObject(index)
                val tags = item.getJSONArray("tags")
                add(
                    SavedPlaceEntry(
                        place = PlaceCandidate(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            category = PlaceCategory.valueOf(item.getString("category")),
                            stayMinutes = item.getInt("stayMinutes"),
                            firstLegMinutes = item.getInt("firstLegMinutes"),
                            secondLegMinutes = item.getInt("secondLegMinutes"),
                            detourMinutes = item.getInt("detourMinutes"),
                            reason = item.optString("reason"),
                            tags = buildList {
                                for (tagIndex in 0 until tags.length()) {
                                    add(tags.getString(tagIndex))
                                }
                            },
                            address = item.optString("address"),
                            imageUrl = item.optString("imageUrl"),
                            latitude = item.optDouble("latitude").takeUnless { it.isNaN() },
                            longitude = item.optDouble("longitude").takeUnless { it.isNaN() },
                            isOpen = item.optBoolean("isOpen", true),
                        ),
                        savedAtMillis = item.getLong("savedAtMillis"),
                    ),
                )
            }
        }.sortedByDescending { it.savedAtMillis }
    }.getOrDefault(emptyList())
}

private fun storeSavedPlaces(
    context: Context,
    entries: List<SavedPlaceEntry>,
) {
    val json = JSONArray().apply {
        entries.forEach { entry ->
            put(
                JSONObject()
                    .put("id", entry.place.id)
                    .put("name", entry.place.name)
                    .put("category", entry.place.category.name)
                    .put("stayMinutes", entry.place.stayMinutes)
                    .put("firstLegMinutes", entry.place.firstLegMinutes)
                    .put("secondLegMinutes", entry.place.secondLegMinutes)
                    .put("detourMinutes", entry.place.detourMinutes)
                    .put("reason", entry.place.reason)
                    .put("tags", JSONArray(entry.place.tags))
                    .put("address", entry.place.address)
                    .put("imageUrl", entry.place.imageUrl)
                    .put("latitude", entry.place.latitude ?: JSONObject.NULL)
                    .put("longitude", entry.place.longitude ?: JSONObject.NULL)
                    .put("isOpen", entry.place.isOpen)
                    .put("savedAtMillis", entry.savedAtMillis),
            )
        }
    }

    context
        .getSharedPreferences(SAVED_PLACES_PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putString(SAVED_PLACES_KEY, json.toString())
        .apply()
}

@Composable
private fun LocationScreen(
    mode: SearchMode,
    startName: String,
    endName: String,
    startLocation: LocationSearchResult?,
    endLocation: LocationSearchResult?,
    searchPlaces: suspend (String) -> List<LocationSearchResult>,
    onModeChange: (SearchMode) -> Unit,
    onStartNameChange: (String) -> Unit,
    onEndNameChange: (String) -> Unit,
    onStartSelected: (LocationSearchResult) -> Unit,
    onEndSelected: (LocationSearchResult) -> Unit,
    onUseStartAsEnd: () -> Unit,
    isChecking: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    MapSheetScreen(onBack = onBack) {
        ModeSelector(mode = mode, onModeChange = onModeChange)
        Spacer(Modifier.height(28.dp))
        Text(
            if (mode == SearchMode.ON_THE_WAY) {
                "어디로 가는 길인가요?"
            } else {
                "어디에서 출발하고,\n어디로 돌아갈까요?"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (mode == SearchMode.ON_THE_WAY) {
                "목적지로 가는 길에 잠깐 들를 곳을 찾아요."
            } else {
                "주변을 둘러본 뒤 원하는 장소에 시간 맞춰 도착해요."
            },
            color = TteumMuted,
        )
        Spacer(Modifier.height(20.dp))
        LocationSearchField(
            value = startName,
            onValueChange = onStartNameChange,
            selected = startLocation,
            label = if (mode == SearchMode.ON_THE_WAY) "출발지" else "시작 위치",
            searchPlaces = searchPlaces,
            onSelected = onStartSelected,
        )
        Spacer(Modifier.height(10.dp))
        LocationSearchField(
            value = endName,
            onValueChange = onEndNameChange,
            selected = endLocation,
            label = if (mode == SearchMode.ON_THE_WAY) "목적지" else "종료 위치",
            searchPlaces = searchPlaces,
            onSelected = onEndSelected,
            highlight = true,
        )
        if (mode == SearchMode.NEARBY) {
            TextButton(onClick = onUseStartAsEnd) {
                Text("시작 위치로 돌아오기")
            }
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = if (isChecking) "위치 확인 중..." else "다음",
            enabled = startName.isNotBlank() && endName.isNotBlank() && !isChecking,
            onClick = onNext,
        )
    }
}

@Composable
private fun LocationSearchField(
    value: String,
    selected: LocationSearchResult?,
    label: String,
    searchPlaces: suspend (String) -> List<LocationSearchResult>,
    onValueChange: (String) -> Unit,
    onSelected: (LocationSearchResult) -> Unit,
    highlight: Boolean = false,
) {
    var results by remember { mutableStateOf(emptyList<LocationSearchResult>()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(value, selected?.id) {
        results = emptyList()
        isLoading = false
        searchMessage = null
        if (value.length < 2 || selected?.name == value || value == "현재 위치") return@LaunchedEffect
        delay(350)
        isLoading = true
        try {
            results = searchPlaces(value)
            if (results.isEmpty()) searchMessage = "검색 결과가 없어요. 장소명을 더 자세히 입력해 주세요."
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            searchMessage = error.message ?: "위치를 검색하지 못했어요."
        } finally {
            isLoading = false
        }
    }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = if (highlight) TteumRed else TteumInk,
                )
            },
            trailingIcon = if (isLoading) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (results.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 5.dp,
            ) {
                Column {
                    results.take(5).forEach { result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(result) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(result.name, fontWeight = FontWeight.Bold)
                            if (result.address.isNotBlank()) {
                                Text(
                                    result.address,
                                    color = TteumMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        } else if (searchMessage != null) {
            Text(
                searchMessage.orEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                color = TteumMuted,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ModeSelector(
    mode: SearchMode,
    onModeChange: (SearchMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        ModeTab(
            label = "가는 길에 들를 곳",
            icon = Icons.Default.DirectionsCar,
            selected = mode == SearchMode.ON_THE_WAY,
            modifier = Modifier.weight(1f),
            onClick = { onModeChange(SearchMode.ON_THE_WAY) },
        )
        ModeTab(
            label = "지금 근처에서 갈 곳",
            icon = Icons.Default.DirectionsWalk,
            selected = mode == SearchMode.NEARBY,
            modifier = Modifier.weight(1f),
            onClick = { onModeChange(SearchMode.NEARBY) },
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(11.dp),
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) TteumInk else TteumMuted)
            Spacer(Modifier.width(8.dp))
            Text(label, color = if (selected) TteumInk else TteumMuted, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TimeScreen(
    mode: SearchMode,
    deadline: Int,
    buffer: Int,
    transport: TransportMode,
    onDeadlineChange: (Int) -> Unit,
    onBufferChange: (Int) -> Unit,
    onTransportChange: (TransportMode) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    var deadlineText by rememberSaveable(deadline) {
        mutableStateOf(deadline.toString())
    }
    val typedDeadline = deadlineText.toIntOrNull()
    val isDeadlineValid = typedDeadline != null &&
        typedDeadline in MIN_DEADLINE_MINUTES..MAX_DEADLINE_MINUTES

    MapSheetScreen(onBack = onBack) {
        Text(
            if (mode == SearchMode.ON_THE_WAY) {
                "목적지 도착까지 얼마나 시간이 남았나요?"
            } else {
                "돌아오기까지 얼마나 시간이 남았나요?"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("15분")
            Text("1시간")
            Text("2시간")
            Text("4시간")
            Text("6시간")
        }
        Slider(
            value = deadline.coerceIn(MIN_DEADLINE_MINUTES, MAX_DEADLINE_MINUTES).toFloat(),
            onValueChange = { value ->
                onDeadlineChange(
                    ((value / 15f).roundToInt() * 15)
                        .coerceIn(MIN_DEADLINE_MINUTES, MAX_DEADLINE_MINUTES),
                )
            },
            valueRange = MIN_DEADLINE_MINUTES.toFloat()..MAX_DEADLINE_MINUTES.toFloat(),
            steps = 22,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = deadlineText,
            onValueChange = { input ->
                if (input.length <= 3 && input.all(Char::isDigit)) {
                    deadlineText = input
                    input.toIntOrNull()
                        ?.takeIf { it in MIN_DEADLINE_MINUTES..MAX_DEADLINE_MINUTES }
                        ?.let(onDeadlineChange)
                }
            },
            label = { Text("남은 시간") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("분") },
            isError = deadlineText.isNotEmpty() && !isDeadlineValid,
            supportingText = if (deadlineText.isEmpty() || isDeadlineValid) {
                null
            } else {
                { Text("15분에서 360분 사이로 입력해 주세요.") }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionTitle("이동수단")
        if (mode == SearchMode.ON_THE_WAY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F2F5), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TteumInk)
                Spacer(Modifier.width(10.dp))
                Text(
                    "가는 길에 들를 장소는 자동차 경로를 기준으로 계산해요.",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SelectButton(
                    text = "도보",
                    icon = Icons.Default.DirectionsWalk,
                    selected = transport == TransportMode.WALK,
                    modifier = Modifier.weight(1f),
                    onClick = { onTransportChange(TransportMode.WALK) },
                )
                SelectButton(
                    text = "차량",
                    icon = Icons.Default.DirectionsCar,
                    selected = transport == TransportMode.CAR,
                    modifier = Modifier.weight(1f),
                    onClick = { onTransportChange(TransportMode.CAR) },
                )
            }
        }

        SectionTitle("늦지 않기 위한 여유")
        ChoiceRow(
            choices = listOf(10 to "10분", 15 to "15분", 20 to "20분", 30 to "30분"),
            selected = buffer,
            onSelect = onBufferChange,
        )
        Text(
            "추천 장소에서 사용할 수 있는 시간은 최대 ${(deadline - buffer).coerceAtLeast(0)}분이에요.",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF1F4), RoundedCornerShape(12.dp))
                .padding(14.dp),
            color = TteumRed,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = "다음",
            enabled = isDeadlineValid,
            onClick = onNext,
        )
    }
}

@Composable
private fun ConditionsScreen(
    mode: SearchMode,
    categories: Set<PlaceCategory>,
    excludeRestaurants: Boolean,
    onToggleCategory: (PlaceCategory) -> Unit,
    onExcludeRestaurantsChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
) {
    MapSheetScreen(onBack = onBack) {
        ModeSelector(mode = mode, onModeChange = {})
        Spacer(Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "어떤 장소를 찾고 있나요?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClear) { Text("조건 없이 찾기") }
        }

        SectionTitle("지금 상태")
        FilterChip(
            selected = excludeRestaurants,
            onClick = { onExcludeRestaurantsChange(!excludeRestaurants) },
            label = { Text("배가 불러요") },
        )
        Text(
            "선택하면 음식점은 추천하지 않아요.",
            color = TteumMuted,
            fontSize = 13.sp,
        )

        SectionTitle("무엇을 하고 싶나요?")
        Text("여러 개를 골라도 돼요.", color = TteumMuted, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PlaceCategory.entries) { category ->
                FilterChip(
                    selected = category in categories,
                    onClick = { onToggleCategory(category) },
                    enabled = !(excludeRestaurants && category == PlaceCategory.RESTAURANT),
                    label = { Text(category.activityLabel()) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = "장소 추천받기", onClick = onSearch)
    }
}

@Composable
private fun LoadingScreen(
    onLoad: suspend () -> Unit,
    onFinished: () -> Unit,
    onBack: () -> Unit,
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attempt by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(attempt) {
        errorMessage = null
        try {
            onLoad()
            delay(500)
            onFinished()
        } catch (error: Exception) {
            errorMessage = error.message ?: "장소를 불러오지 못했습니다."
        }
    }
    Box(Modifier.fillMaxSize()) {
        MapBackground(Modifier.fillMaxSize())
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(28.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = Color(0xFFFFE7ED),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TteumRed,
                        modifier = Modifier.padding(18.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                if (errorMessage == null) {
                    Text(
                        "시간 안에 다녀올 수 있는\n장소를 찾고 있어요.",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(16.dp))
                    listOf("출발지와 목적지 확인", "실시간 이동 시간 계산", "갈 만한 장소 확인", "도착 전 여유 시간 적용").forEach {
                        Text(
                            "✓  $it",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = TteumMuted,
                        )
                    }
                } else {
                    Text(
                        "장소를 추천하지 못했어요.",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(errorMessage.orEmpty(), color = TteumMuted)
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "다시 시도", onClick = { attempt += 1 })
                    TextButton(onClick = onBack) {
                        Text("조건으로 돌아가기")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsScreen(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSelect: (SafeRecommendation) -> Unit,
) {
    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${criteria.startName} → ${criteria.endName}", fontWeight = FontWeight.Bold)
                        Text("${formatMinutes(criteria.deadlineMinutesFromNow)} 내 · 도착 전 여유 ${criteria.safetyBufferMinutes}분", color = TteumMuted, fontSize = 13.sp)
                    }
                    TextButton(onClick = onEdit) { Text("조건 수정") }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RouteMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    latitude = recommendations.firstOrNull()?.place?.latitude
                        ?: criteria.endCoordinates?.latitude,
                    longitude = recommendations.firstOrNull()?.place?.longitude
                        ?: criteria.endCoordinates?.longitude,
                )
            }
            item {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    Text(
                        if (recommendations.isEmpty()) "시간 안에 다녀올 장소가 없어요" else "${recommendations.size}개의 장소를 찾았어요",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (recommendations.isEmpty()) {
                            "도착 시간을 늦추거나 조건을 줄여 다시 찾아보세요."
                        } else {
                            "남은 시간과 도착 전 여유를 모두 반영했어요."
                        },
                        color = TteumMuted,
                    )
                    recommendationConditionSummary(criteria.categories)?.let { summary ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "반영한 선택: $summary",
                            color = TteumRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (recommendations.isEmpty()) {
                item {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        Text("조건 다시 고르기")
                    }
                }
            } else {
                itemsIndexed(recommendations, key = { _, item -> item.place.id }) { index, recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        position = index + 1,
                        total = recommendations.size,
                        modifier = Modifier.padding(horizontal = 18.dp),
                        onClick = { onSelect(recommendation) },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: SafeRecommendation,
    position: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "${position}번 선택지 [$position/$total]",
                color = TteumMuted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recommendation.place.category.label, color = TteumRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(recommendation.place.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                SafetyBadge(recommendation)
            }
            Spacer(Modifier.height(12.dp))
            Text(recommendation.place.reason, color = TteumMuted)
            Spacer(Modifier.height(15.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Metric("머무는 시간", "${recommendation.place.stayMinutes}분")
                Metric("전체 예상", "${recommendation.totalMinutes}분")
                Metric("남는 시간", "${recommendation.marginMinutes}분", TteumRed)
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recommendation.place.tags) { tag ->
                    Surface(color = Color(0xFFF1F2F5), shape = RoundedCornerShape(50)) {
                        Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("상세 보기", color = TteumRed, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TteumRed)
            }
        }
    }
}

@Composable
private fun DetailScreen(
    criteria: SearchCriteria,
    recommendation: SafeRecommendation,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        if (criteria.mode == SearchMode.ON_THE_WAY) {
                            openKakaoMapRoute(
                                context = context,
                                start = criteria.startCoordinates,
                                waypoint = recommendation.place.latitude?.let { latitude ->
                                    recommendation.place.longitude?.let { longitude ->
                                        Coordinates(latitude, longitude)
                                    }
                                },
                                destination = criteria.endCoordinates,
                                transport = TransportMode.CAR,
                            )
                        } else {
                            openKakaoMapRoute(
                                context = context,
                                start = criteria.startCoordinates,
                                waypoint = null,
                                destination = recommendation.place.latitude?.let { latitude ->
                                    recommendation.place.longitude?.let { longitude ->
                                        Coordinates(latitude, longitude)
                                    }
                                },
                                transport = criteria.transportMode,
                            )
                        }
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (criteria.mode == SearchMode.ON_THE_WAY) {
                            "카카오맵으로 들르는 길 안내"
                        } else {
                            "카카오맵으로 길 안내"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                Box {
                    RouteMap(
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                        latitude = recommendation.place.latitude,
                        longitude = recommendation.place.longitude,
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(14.dp)
                            .background(Color.White, CircleShape),
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            }
            item {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(recommendation.place.category.label, color = TteumRed, fontWeight = FontWeight.Bold)
                            Text(recommendation.place.name, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onToggleSave) {
                            Icon(
                                if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isSaved) "저장 해제" else "저장",
                                tint = if (isSaved) TteumRed else TteumMuted,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(recommendation.place.reason, color = TteumMuted)
                    Spacer(Modifier.height(20.dp))
                    SafetySummary(recommendation)
                    SectionTitle("시간 예상")
                    TimelineRow(criteria.startName, "${recommendation.place.firstLegMinutes}분 이동")
                    TimelineRow(recommendation.place.name, "${recommendation.place.stayMinutes}분 머물기", highlighted = true)
                    TimelineRow(criteria.endName, "${recommendation.place.secondLegMinutes}분 이동")
                    SectionTitle("장소 정보")
                    Text("영업시간은 방문 전에 확인해 주세요.", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(recommendation.place.tags.joinToString(" · "), color = TteumMuted)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "이동시간과 영업 정보는 실시간 상황에 따라 달라질 수 있어요. 출발 전 최신 정보를 확인해 주세요.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF6E7), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        color = Color(0xFF765A22),
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun SafetySummary(
    recommendation: SafeRecommendation,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F4)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "시간 안에 가능 · ${recommendation.marginMinutes}분 남아요",
                color = TteumRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "이동과 머무는 시간은 총 ${recommendation.totalMinutes}분으로 예상돼요.",
                color = TteumInk,
            )
        }
    }
}

@Composable
private fun TimelineRow(
    title: String,
    detail: String,
    highlighted: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(if (highlighted) TteumRed else Color(0xFFA9ADB5), CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium)
        Text(detail, color = if (highlighted) TteumRed else TteumMuted)
    }
}

@Composable
private fun SafetyBadge(recommendation: SafeRecommendation) {
    val background = when (recommendation.safetyLevel) {
        SafetyLevel.COMFORTABLE -> Color(0xFFE8F7EE)
        SafetyLevel.AVAILABLE -> Color(0xFFFFF6E0)
        SafetyLevel.TIGHT -> Color(0xFFFFE9ED)
    }
    val foreground = when (recommendation.safetyLevel) {
        SafetyLevel.COMFORTABLE -> Color(0xFF197342)
        SafetyLevel.AVAILABLE -> Color(0xFF8A5C00)
        SafetyLevel.TIGHT -> TteumRed
    }
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(
            "${recommendation.safetyLevel.label} ${recommendation.marginMinutes}분",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = foreground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Metric(label: String, value: String, valueColor: Color = TteumInk) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TteumMuted, fontSize = 12.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MapSheetScreen(
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        MapBackground(Modifier.fillMaxSize())
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.72f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 10.dp,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(20.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                        }
                        Text("장소 찾기", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Column(content = content)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 5.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(text, color = TteumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RoundMapButton(
    onClick: (() -> Unit)?,
    icon: @Composable () -> Unit,
    background: Color = Color.White,
    foreground: Color = TteumInk,
    border: BorderStroke? = null,
) {
    Surface(
        modifier = Modifier
            .size(52.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        color = background,
        contentColor = foreground,
        shape = CircleShape,
        border = border,
        shadowElevation = 5.dp,
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
}

@Composable
private fun <T> ChoiceRow(
    choices: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(choices) { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun SelectButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Color(0xFFFFEDF1) else Color.Transparent,
            contentColor = if (selected) TteumRed else TteumInk,
        ),
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(7.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MapBackground(
    modifier: Modifier = Modifier,
    requestedLocation: RequestedMapLocation? = null,
) {
    KakaoMapSurface(
        modifier = modifier,
        latitude = 37.7645,
        longitude = 128.8996,
        zoomLevel = 13,
        requestedLocation = requestedLocation,
    )
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFFEDEDE8))) {
        val road = Color.White
        val minorRoad = Color(0xFFF7F7F4)
        repeat(7) { index ->
            val y = size.height * (0.12f + index * 0.13f)
            drawLine(
                color = road,
                start = Offset(0f, y),
                end = Offset(size.width, y - size.height * 0.10f),
                strokeWidth = 18f,
                cap = StrokeCap.Round,
            )
        }
        repeat(5) { index ->
            val x = size.width * (0.1f + index * 0.2f)
            drawLine(
                color = minorRoad,
                start = Offset(x, 0f),
                end = Offset(x + size.width * 0.18f, size.height),
                strokeWidth = 10f,
            )
        }
        drawCircle(TteumRed, radius = 14f, center = Offset(size.width * 0.5f, size.height * 0.46f))
        drawCircle(Color(0x55EF003D), radius = 28f, center = Offset(size.width * 0.5f, size.height * 0.46f))
    }
}

@Composable
private fun RouteMap(
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null,
) {
    KakaoMapSurface(
        modifier = modifier,
        latitude = latitude ?: 37.7645,
        longitude = longitude ?: 128.8996,
        zoomLevel = 15,
    )
}

@Composable
private fun KakaoMapSurface(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoomLevel: Int,
    requestedLocation: RequestedMapLocation? = null,
) {
    if (BuildConfig.KAKAO_MAP_NATIVE_APP_KEY.isBlank()) {
        Box(modifier) {
            MapPlaceholder(Modifier.fillMaxSize())
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "카카오맵 앱 키 설정이 필요합니다.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = TteumMuted,
                    fontSize = 13.sp,
                )
            }
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLocationLabel by remember { mutableStateOf<Label?>(null) }

    DisposableEffect(mapView, lifecycleOwner) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(error: Exception) {
                    mapError = error.message ?: "카카오맵을 불러오지 못했습니다."
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(readyMap: KakaoMap) {
                    kakaoMap = readyMap
                }

                override fun getPosition(): LatLng =
                    LatLng.from(latitude, longitude)

                override fun getZoomLevel(): Int = zoomLevel
            },
        )

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.resume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentLocationLabel = null
            kakaoMap = null
            mapView.pause()
            mapView.finish()
        }
    }

    LaunchedEffect(kakaoMap, requestedLocation?.requestId) {
        val map = kakaoMap ?: return@LaunchedEffect
        val target = requestedLocation
        if (target == null) {
            currentLocationLabel?.remove()
            currentLocationLabel = null
            return@LaunchedEffect
        }
        val position = LatLng.from(target.latitude, target.longitude)

        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(position, 16),
            CameraAnimation.from(500),
        )

        val existingLabel = currentLocationLabel
        if (existingLabel == null) {
            currentLocationLabel = map.labelManager?.layer?.addLabel(
                LabelOptions.from("current-location", position)
                    .setStyles(createCurrentLocationMarkerBitmap(context)),
            )
        } else {
            existingLabel.moveTo(position, 350)
        }
    }

    Box(modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )
        mapError?.let { message ->
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = Color.White.copy(alpha = 0.94f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    message,
                    modifier = Modifier.padding(16.dp),
                    color = TteumMuted,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun openKakaoMap(context: Context, placeName: String) {
    val encoded = Uri.encode(placeName)
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("kakaomap://search?q=$encoded"))
    try {
        context.startActivity(appIntent)
    } catch (_: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://map.kakao.com/link/search/$encoded"),
        )
        try {
            context.startActivity(webIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "지도를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun openKakaoMapRoute(
    context: Context,
    start: Coordinates?,
    waypoint: Coordinates?,
    destination: Coordinates?,
    transport: TransportMode,
) {
    if (start == null || destination == null) {
        Toast.makeText(
            context,
            "경로 좌표를 확인할 수 없어 카카오맵을 열지 못했어요.",
            Toast.LENGTH_SHORT,
        ).show()
        return
    }

    val routeQuery = buildKakaoMapRouteQuery(start, destination, transport, waypoint)
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("kakaomap://route?$routeQuery"),
    )

    try {
        context.startActivity(appIntent)
    } catch (_: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://m.map.kakao.com/scheme/route?$routeQuery"),
        )
        try {
            context.startActivity(webIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "지도를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

internal fun buildKakaoMapRouteQuery(
    start: Coordinates,
    destination: Coordinates,
    transport: TransportMode,
    waypoint: Coordinates? = null,
): String =
    buildString {
        append("sp=${start.latitude},${start.longitude}")
        waypoint?.let { append("&vp=${it.latitude},${it.longitude}") }
        append("&ep=${destination.latitude},${destination.longitude}")
        append("&by=${if (transport == TransportMode.CAR) "car" else "foot"}")
    }
