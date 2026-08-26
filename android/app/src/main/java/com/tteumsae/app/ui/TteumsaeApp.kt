package com.tteumsae.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.CompetitionType
import com.kakao.vectormap.label.CompetitionUnit
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.OrderingType
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyles
import com.kakao.vectormap.shape.PolygonStylesSet
import com.tteumsae.app.BuildConfig
import com.tteumsae.app.data.TteumsaeApi
import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.LocationSearchResult
import com.tteumsae.app.domain.OperationStatus
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SafetyLevel
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.domain.SearchMode
import com.tteumsae.app.domain.TransportMode
import com.tteumsae.app.domain.recommendation.RecommendationIntent
import com.tteumsae.app.domain.recommendation.matchesGangwonRegion
import com.tteumsae.app.domain.recommendation.recommendationCategories
import com.tteumsae.app.domain.recommendation.recommendationIntentFilters
import com.tteumsae.app.domain.recommendation.selectedRecommendationIntent
import com.tteumsae.app.domain.recommendation.toggleRecommendationIntent
import com.tteumsae.app.domain.route.additionalDetourDistanceMeters
import com.tteumsae.app.domain.route.isRouteWithinExtraTimeBudget
import com.tteumsae.app.domain.route.orderWaypointIdsAlongRoute
import com.tteumsae.app.domain.route.selectedRouteEstimate
import com.tteumsae.app.platform.CONTACT_EMAIL
import com.tteumsae.app.platform.LOCATION_TERMS_URL
import com.tteumsae.app.platform.MAX_KAKAO_WAYPOINTS
import com.tteumsae.app.platform.PRIVACY_POLICY_URL
import com.tteumsae.app.platform.clearAppCache
import com.tteumsae.app.platform.isKakaoMapAvailable
import com.tteumsae.app.platform.openAppSettings
import com.tteumsae.app.platform.openContactEmail
import com.tteumsae.app.platform.openKakaoMap
import com.tteumsae.app.platform.openKakaoMapHome
import com.tteumsae.app.platform.openKakaoMapInstallPage
import com.tteumsae.app.platform.openKakaoMapMultiRoute
import com.tteumsae.app.platform.openKakaoMapRoute
import com.tteumsae.app.platform.openPolicy
import com.tteumsae.app.platform.savedImageCache
import com.tteumsae.app.ui.common.compactTags
import com.tteumsae.app.ui.common.formatDistance
import com.tteumsae.app.ui.common.formatMinutes
import com.tteumsae.app.ui.navigation.AppDestination
import com.tteumsae.app.ui.navigation.MainTab
import com.tteumsae.app.ui.navigation.previousDestination
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class SavedSort {
    DEFAULT,
    NAME,
}

private val gangwonRegionCodes = linkedMapOf(
    "강원도 전체" to null,
    "강릉" to 1,
    "고성" to 2,
    "동해" to 3,
    "삼척" to 4,
    "속초" to 5,
    "양구" to 6,
    "양양" to 7,
    "영월" to 8,
    "원주" to 9,
    "인제" to 10,
    "정선" to 11,
    "철원" to 12,
    "춘천" to 13,
    "태백" to 14,
    "평창" to 15,
    "홍천" to 16,
    "화천" to 17,
    "횡성" to 18,
)

private const val MIN_DEADLINE_MINUTES = 15
private const val SLIDER_MAX_DEADLINE_MINUTES = 360
private const val MAX_DEADLINE_MINUTES = 1440
private const val DEFAULT_EXTRA_TIME_MINUTES = MAX_DEADLINE_MINUTES
private val RouteFocusBlue = Color(0xFF2F6FE4)

private data class MapCandidate(
    val id: String,
    val coordinates: Coordinates,
    val category: PlaceCategory,
    val selectedOrder: Int?,
    val isFocused: Boolean,
)

internal fun deniedLocationPermissionNeedsSettings(
    permissions: Map<String, Boolean>,
    canAskAgain: (String) -> Boolean,
): Boolean = permissions.any { (permission, granted) ->
    !granted && !canAskAgain(permission)
}

internal fun networkFailureMessage(operation: String, detail: String?): String =
    "${operation}에 실패했어요. ${detail?.takeIf(String::isNotBlank) ?: "네트워크 연결을 확인해 주세요."}"

internal fun shouldAutoLocateStart(startName: String, hasLocation: Boolean): Boolean =
    startName == "현재 위치" && !hasLocation
private const val HOME_INTRO_PREFERENCES = "home_intro"
private const val HOME_INTRO_HIDDEN_DATE = "hidden_date"

private data class SavedPlaceEntry(
    val place: PlaceCandidate,
    val savedAtMillis: Long,
)

private data class PlaceCardItem(
    val place: PlaceCandidate,
    val savedEntry: SavedPlaceEntry?,
)

@Composable
fun TteumsaeApp() {
    val context = LocalContext.current
    val api = remember { TteumsaeApi() }
    var screen by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var mode by rememberSaveable { mutableStateOf(SearchMode.ON_THE_WAY) }
    var startName by rememberSaveable { mutableStateOf("현재 위치") }
    var endName by rememberSaveable { mutableStateOf("") }
    var startLocation by remember { mutableStateOf<LocationSearchResult?>(null) }
    var endLocation by remember { mutableStateOf<LocationSearchResult?>(null) }
    var deadline by rememberSaveable { mutableStateOf(DEFAULT_EXTRA_TIME_MINUTES) }
    var buffer by rememberSaveable { mutableStateOf(15) }
    var transport by rememberSaveable { mutableStateOf(TransportMode.CAR) }
    var categories by remember { mutableStateOf(emptySet<PlaceCategory>()) }
    var excludeRestaurants by rememberSaveable { mutableStateOf(false) }
    var selectedIntents by remember { mutableStateOf(setOf(RecommendationIntent.ANY)) }
    var recommendations by remember { mutableStateOf(emptyList<SafeRecommendation>()) }
    var recommendationWarning by remember { mutableStateOf("") }
    var baseRoute by remember { mutableStateOf<RouteSummary?>(null) }
    var corridorRadiusMeters by remember { mutableStateOf(1_600) }
    var selectedWaypointIds by remember { mutableStateOf(emptyList<String>()) }
    var selected by remember { mutableStateOf<SafeRecommendation?>(null) }
    var detailRouteChecking by remember { mutableStateOf(false) }
    var activeCriteria by remember { mutableStateOf<SearchCriteria?>(null) }
    var locationChecking by remember { mutableStateOf(false) }
    var currentLocationTarget by remember { mutableStateOf<RequestedMapLocation?>(null) }
    var routeSummaryExpanded by rememberSaveable { mutableStateOf(true) }
    var focusedResultPlaceId by rememberSaveable { mutableStateOf<String?>(null) }
    var savedPlaces by remember { mutableStateOf(loadSavedPlaces(context)) }
    var catalogPlaces by remember { mutableStateOf(emptyList<PlaceCandidate>()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogLoadingMore by remember { mutableStateOf(false) }
    var catalogPage by remember { mutableStateOf(1) }
    var catalogHasMore by remember { mutableStateOf(true) }
    var catalogRegion by rememberSaveable { mutableStateOf("강릉") }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var catalogLoadAttempt by rememberSaveable { mutableStateOf(0) }
    var showHomeIntro by rememberSaveable { mutableStateOf(shouldShowHomeIntro(context)) }
    val appScope = rememberCoroutineScope()

    BackHandler(enabled = screen != AppDestination.HOME) {
        screen = previousDestination(screen)
    }
    val updateSavedPlaces: (List<SavedPlaceEntry>) -> Unit = { updated ->
        savedPlaces = updated
        storeSavedPlaces(context, updated)
    }

    LaunchedEffect(screen, catalogLoadAttempt, catalogRegion) {
        if (screen == AppDestination.SAVED) {
            catalogLoading = true
            catalogError = null
            try {
                val firstPage = api.places(sigunguCode = gangwonRegionCodes[catalogRegion])
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
        startName = startName.ifBlank { "현재 위치" },
        endName = endName.ifBlank { if (mode == SearchMode.NEARBY) startName else "" },
        deadlineMinutesFromNow = deadline,
        safetyBufferMinutes = buffer,
        transportMode = if (mode == SearchMode.ON_THE_WAY) TransportMode.CAR else transport,
        categories = recommendationCategories(categories, excludeRestaurants),
        startCoordinates = startLocation?.coordinates,
        endCoordinates = endLocation?.coordinates,
    )
    val openRoute: (List<SafeRecommendation>) -> Unit = { routeRecommendations ->
        val resolved = activeCriteria ?: criteria
        openKakaoMapMultiRoute(
            context = context,
            start = resolved.startCoordinates,
            startName = resolved.startName,
            waypoints = routeRecommendations.mapNotNull { recommendation ->
                recommendation.place.latitude?.let { latitude ->
                    recommendation.place.longitude?.let { longitude ->
                        recommendation.place.name to Coordinates(latitude, longitude)
                    }
                }
            },
            destination = resolved.endCoordinates,
            destinationName = resolved.endName,
        )
    }

    when (screen) {
        AppDestination.HOME -> HomeScreen(
            showIntro = showHomeIntro,
            currentLocationTarget = currentLocationTarget,
            onCurrentLocationTargetChange = { currentLocationTarget = it },
            onDismissIntro = { hideForToday ->
                if (hideForToday) hideHomeIntroForToday(context)
                showHomeIntro = false
            },
            onStart = { coordinates ->
                mode = SearchMode.ON_THE_WAY
                deadline = DEFAULT_EXTRA_TIME_MINUTES
                buffer = 15
                selectedWaypointIds = emptyList()
                startName = "현재 위치"
                val currentLocation = coordinates?.let {
                    LocationSearchResult(
                        id = "current-location",
                        name = "현재 위치",
                        address = "GPS로 확인한 위치",
                        coordinates = it,
                    )
                }
                startLocation = currentLocation
                endName = ""
                endLocation = null
                screen = AppDestination.LOCATION
            },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppDestination.HOME
                    MainTab.SAVED -> AppDestination.SAVED
                    MainTab.SETTINGS -> AppDestination.SETTINGS
                }
            },
        )

        AppDestination.SAVED -> SavedPlacesScreen(
            catalogPlaces = catalogPlaces,
            savedPlaces = savedPlaces,
            selectedRegion = catalogRegion,
            onRegionSelected = { catalogRegion = it },
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
                            val nextPage = api.places(
                                page = catalogPage + 1,
                                sigunguCode = gangwonRegionCodes[catalogRegion],
                            )
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
                    MainTab.EXPLORE -> AppDestination.HOME
                    MainTab.SAVED -> AppDestination.SAVED
                    MainTab.SETTINGS -> AppDestination.SETTINGS
                }
            },
        )

        AppDestination.SETTINGS -> SettingsTabScreen(
            savedCount = savedPlaces.size,
            onClearSaved = { updateSavedPlaces(emptyList()) },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppDestination.HOME
                    MainTab.SAVED -> AppDestination.SAVED
                    MainTab.SETTINGS -> AppDestination.SETTINGS
                }
            },
        )

        AppDestination.LOCATION -> LocationScreen(
            startName = startName,
            endName = endName,
            startLocation = startLocation,
            endLocation = endLocation,
            searchPlaces = api::searchPlaces,
            resolveCurrentAddress = api::regionAddress,
            onStartNameChange = {
                startName = it
                startLocation = null
                currentLocationTarget = null
            },
            onEndNameChange = {
                endName = it
                endLocation = null
            },
            onStartSelected = {
                startName = it.name
                startLocation = it
                currentLocationTarget = if (it.id == "current-location") {
                    RequestedMapLocation(
                        latitude = it.coordinates.latitude,
                        longitude = it.coordinates.longitude,
                        requestId = System.nanoTime(),
                    )
                } else {
                    null
                }
            },
            onEndSelected = {
                endName = it.name
                endLocation = it
            },
            isChecking = locationChecking,
            onBack = { screen = AppDestination.HOME },
            onNext = {
                if (!locationChecking) {
                    locationChecking = true
                    appScope.launch {
                        try {
                            val resolvedStart = startLocation ?: api.searchPlace(
                                startName,
                                gangwonOnly = mode == SearchMode.NEARBY,
                            )
                            val resolvedEnd = if (mode == SearchMode.NEARBY) {
                                resolvedStart
                            } else {
                                endLocation ?: api.searchPlace(endName, gangwonOnly = true)
                            }
                            val gangwonLocation = if (mode == SearchMode.NEARBY) resolvedStart else resolvedEnd
                            if (!api.isGangwon(gangwonLocation.coordinates)) {
                                Toast.makeText(
                                    context,
                                    if (mode == SearchMode.NEARBY) {
                                        "현재 위치는 강원도 밖이라 근처 장소를 추천할 수 없어요."
                                    } else {
                                        "목적지가 강원도 밖이라 이동 중 들를 장소를 추천할 수 없어요."
                                    },
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                startName = resolvedStart.name
                                startLocation = resolvedStart
                                endName = resolvedEnd.name
                                endLocation = resolvedEnd
                                screen = AppDestination.CONDITIONS
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

        AppDestination.CONDITIONS -> ConditionsScreen(
            selectedIntents = selectedIntents,
            onIntentSelected = { intent ->
                selectedIntents = toggleRecommendationIntent(selectedIntents, intent)
                val (selectedCategories, excludeFood) = recommendationIntentFilters(selectedIntents)
                categories = selectedCategories
                excludeRestaurants = excludeFood
            },
            onBack = { screen = AppDestination.LOCATION },
            onSearch = { screen = AppDestination.LOADING },
        )

        AppDestination.LOADING -> LoadingScreen(
            onLoad = {
                val start = startLocation ?: api.searchPlace(
                    criteria.startName,
                    gangwonOnly = criteria.mode == SearchMode.NEARBY,
                )
                val end = if (criteria.mode == SearchMode.NEARBY) {
                    start
                } else {
                    endLocation ?: api.searchPlace(criteria.endName, gangwonOnly = true)
                }
                val resolvedCriteria = criteria.copy(
                    startName = start.name,
                    endName = end.name,
                    startCoordinates = start.coordinates,
                    endCoordinates = end.coordinates,
                )
                val result = api.recommendations(resolvedCriteria)
                recommendations = result.recommendations
                recommendationWarning = result.warning
                baseRoute = result.baseRoute
                corridorRadiusMeters = result.corridorRadiusMeters
                selectedWaypointIds = emptyList()
                focusedResultPlaceId = result.recommendations.firstOrNull()?.place?.id
                activeCriteria = resolvedCriteria
            },
            onFinished = {
                routeSummaryExpanded = true
                screen = AppDestination.RESULTS
            },
            onBack = { screen = AppDestination.CONDITIONS },
        )

        AppDestination.RESULTS -> ResultsScreen(
            criteria = activeCriteria ?: criteria,
            recommendations = recommendations,
            warning = recommendationWarning,
            baseRoute = baseRoute,
            corridorRadiusMeters = corridorRadiusMeters,
            currentLocationTarget = currentLocationTarget,
            onCurrentLocationTargetChange = { currentLocationTarget = it },
            summaryExpanded = routeSummaryExpanded,
            onSummaryExpandedChange = { routeSummaryExpanded = it },
            focusedPlaceId = focusedResultPlaceId,
            onFocusedPlaceIdChange = { focusedResultPlaceId = it },
            selectedIds = selectedWaypointIds,
            onSelectedIdsChange = { selectedWaypointIds = it },
            calculateRoute = { waypoints ->
                val resolved = activeCriteria ?: criteria
                val start = requireNotNull(resolved.startCoordinates)
                val destination = requireNotNull(resolved.endCoordinates)
                api.route(start, destination, waypoints)
            },
            onBack = { screen = AppDestination.CONDITIONS },
            onClearConditions = {
                categories = emptySet()
                excludeRestaurants = false
                selectedIntents = setOf(RecommendationIntent.ANY)
                screen = AppDestination.LOADING
            },
            onSearchOtherPlace = { screen = AppDestination.LOCATION },
            onOpenRoute = openRoute,
            onSelect = {
                selected = it
                screen = AppDestination.DETAIL
            },
        )

        AppDestination.DETAIL -> selected?.let {
            val selectedRecommendations = selectedWaypointIds.mapNotNull { selectedId ->
                recommendations.find { recommendation -> recommendation.place.id == selectedId }
            }
            val isReplacingLastWaypoint = it.place.id !in selectedWaypointIds &&
                selectedRecommendations.size >= MAX_KAKAO_WAYPOINTS
            val detailRoute = if (isReplacingLastWaypoint) {
                selectedRecommendations.dropLast(1) + it
            } else {
                (selectedRecommendations + it).distinctBy { recommendation -> recommendation.place.id }
            }
            DetailScreen(
                criteria = activeCriteria ?: criteria,
                recommendation = it,
                warning = recommendationWarning,
                isSaved = savedPlaces.any { entry -> entry.place.id == it.place.id },
                onToggleSave = {
                    updateSavedPlaces(if (savedPlaces.any { entry -> entry.place.id == it.place.id }) {
                        savedPlaces.filterNot { entry -> entry.place.id == it.place.id }
                    } else {
                        listOf(SavedPlaceEntry(it.place, System.currentTimeMillis())) + savedPlaces
                    })
                },
                isOpeningRoute = detailRouteChecking,
                onOpenRoute = openDetailRoute@{
                    if (detailRouteChecking) return@openDetailRoute
                    detailRouteChecking = true
                    appScope.launch {
                        try {
                            val resolved = activeCriteria ?: criteria
                            val start = resolved.startCoordinates
                            val destination = resolved.endCoordinates
                            if (start == null || destination == null) {
                                Toast.makeText(
                                    context,
                                    "경로 좌표를 확인할 수 없어요.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                return@launch
                            }
                            val calculated = runCatching {
                                api.route(start, destination, detailRoute.map { recommendation -> recommendation.place })
                            }
                            val detailRouteSummary = calculated.getOrElse {
                                Toast.makeText(
                                    context,
                                    "실시간 경로를 확인하지 못해 예상 경로로 판단해요.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                fallbackRouteSummary(resolved, detailRoute)
                            }
                            val directRoute = baseRoute ?: fallbackBaseRouteSummary(resolved, recommendations)
                            val isWithinBudget = isRouteWithinExtraTimeBudget(
                                baseDrivingMinutes = directRoute.totalDrivingMinutes,
                                extraTimeMinutes = resolved.deadlineMinutesFromNow,
                                selectedDrivingMinutes = detailRouteSummary.totalDrivingMinutes,
                                selectedStayMinutes = detailRoute.sumOf { recommendation -> recommendation.place.stayMinutes },
                                safetyBufferMinutes = resolved.safetyBufferMinutes,
                            )
                            if (!isWithinBudget) {
                                Toast.makeText(
                                    context,
                                    "이 경로는 추천 가능한 범위를 초과해요. 다른 장소를 골라주세요.",
                                    Toast.LENGTH_LONG,
                                ).show()
                                return@launch
                            }
                            if (isReplacingLastWaypoint) {
                                Toast.makeText(
                                    context,
                                    "마지막으로 추가한 경유지를 이 장소로 바꿔 안내해요.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                            selectedWaypointIds = detailRoute.map { recommendation -> recommendation.place.id }
                            openRoute(detailRoute)
                        } finally {
                            detailRouteChecking = false
                        }
                    }
                },
                onBack = { screen = AppDestination.RESULTS },
            )
        } ?: run {
            screen = AppDestination.RESULTS
        }
    }
}

@Composable
private fun HomeScreen(
    showIntro: Boolean,
    currentLocationTarget: RequestedMapLocation?,
    onCurrentLocationTargetChange: (RequestedMapLocation?) -> Unit,
    onDismissIntro: (Boolean) -> Unit,
    onStart: (Coordinates?) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val context = LocalContext.current
    var isLocating by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    val locateCurrentPosition: () -> Unit = {
        isLocating = true
        requestCurrentLocation(
            context = context,
            onSuccess = { location ->
                onCurrentLocationTargetChange(
                    RequestedMapLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        requestId = System.nanoTime(),
                    ),
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
        } else if (deniedLocationPermissionNeedsSettings(permissions) { permission ->
                (context as? android.app.Activity)
                    ?.shouldShowRequestPermissionRationale(permission) == true
            }
        ) {
            showPermissionSettingsDialog = true
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
                text = "목적지 검색",
                onClick = {
                    onStart(
                        currentLocationTarget?.let {
                            Coordinates(it.latitude, it.longitude)
                        },
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
            ) {
                RoundMapButton(
                    onClick = if (isLocating) null else {
                        {
                            if (currentLocationTarget != null) {
                                onCurrentLocationTargetChange(null)
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
                    contentDescription = "현재 위치 표시",
                    selected = currentLocationTarget != null,
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
                                contentDescription = null,
                            )
                        }
                    },
                )
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

    if (showPermissionSettingsDialog) {
        LocationPermissionSettingsDialog(
            onDismiss = { showPermissionSettingsDialog = false },
            onOpenSettings = {
                showPermissionSettingsDialog = false
                openAppSettings(context)
            },
        )
    }

    if (showIntro) {
        HomeIntroDialog(onDismiss = onDismissIntro)
    }
}

@Composable
private fun HomeIntroDialog(onDismiss: (Boolean) -> Unit) {
    var hideForToday by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "강원도로 가는 길, 어디 들를까?",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "기본 경로와 추가 이동·머무는 시간을 계산해\n가는 길에 들를 장소를 찾아드려요.",
                    color = TteumMuted,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.clickable { hideForToday = !hideForToday },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = hideForToday,
                        onCheckedChange = { hideForToday = it },
                    )
                    Text("오늘 하루 보지 않기", color = TteumMuted)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onDismiss(hideForToday) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("확인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val mapSelected = selectedTab == MainTab.EXPLORE
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 92.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            BottomNavItem(
                icon = Icons.Default.FormatListBulleted,
                label = "틈새 발견",
                selected = selectedTab == MainTab.SAVED,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(MainTab.SAVED) },
            )
                Spacer(Modifier.weight(1f))
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "설정",
                selected = selectedTab == MainTab.SETTINGS,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(MainTab.SETTINGS) },
            )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(58.dp)
                    .selectable(
                        selected = mapSelected,
                        role = Role.Tab,
                        onClick = { onTabSelected(MainTab.EXPLORE) },
                    ),
                color = if (mapSelected) TteumRed else Color(0xFFF4F5F7),
                contentColor = if (mapSelected) Color.White else Color(0xFF6F747D),
                shape = CircleShape,
                shadowElevation = if (mapSelected) 5.dp else 0.dp,
                border = BorderStroke(
                    5.dp,
                    if (mapSelected) Color(0xFFE4E5E8) else Color(0xFFD9DCE1),
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "지도 홈",
                        modifier = Modifier.size(27.dp),
                    )
                }
            }
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
    val color = if (selected) TteumInk else TteumMuted
    Column(
        modifier = modifier.selectable(
            selected = selected,
            role = Role.Tab,
            onClick = onClick,
        ),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SavedPlacesScreen(
    catalogPlaces: List<PlaceCandidate>,
    savedPlaces: List<SavedPlaceEntry>,
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
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
                        gangwonRegionCodes.keys.forEach { region ->
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

@Composable
private fun SettingsTabScreen(
    savedCount: Int,
    onClearSaved: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var kakaoMapAvailable by remember { mutableStateOf(isKakaoMapAvailable(context)) }
    var showSavedClearDialog by remember { mutableStateOf(false) }
    var showCacheClearDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermissionGranted = hasLocationPermission(context)
                kakaoMapAvailable = isKakaoMapAvailable(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        bottomBar = {
            BottomNavigation(
                selectedTab = MainTab.SETTINGS,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("설정", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                SettingsSectionTitle("앱 사용")
                SettingsGroup {
                    SettingsRow(
                        title = "위치 권한",
                        description = if (locationPermissionGranted) "허용됨" else "허용되지 않음",
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}"),
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "카카오맵",
                        description = if (kakaoMapAvailable) "설치됨 · 눌러서 실행" else "설치 필요",
                        onClick = {
                            if (kakaoMapAvailable) {
                                openKakaoMapHome(context)
                            } else {
                                openKakaoMapInstallPage(context)
                            }
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("저장 공간")
                SettingsGroup {
                    SettingsRow(
                        title = "캐시 지우기",
                        description = "임시 이미지와 지도 데이터를 정리해요.",
                        onClick = { showCacheClearDialog = true },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "저장한 장소 비우기",
                        description = "현재 ${savedCount}개 저장됨",
                        titleColor = if (savedCount > 0) TteumRed else TteumMuted,
                        onClick = if (savedCount > 0) {
                            { showSavedClearDialog = true }
                        } else {
                            null
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("약관 및 지원")
                SettingsGroup {
                    SettingsRow(
                        title = "개인정보처리방침",
                        description = if (PRIVACY_POLICY_URL.isBlank()) "준비 중" else "보기",
                        onClick = { openPolicy(context, PRIVACY_POLICY_URL) },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "위치기반서비스 이용약관",
                        description = if (LOCATION_TERMS_URL.isBlank()) "준비 중" else "보기",
                        onClick = { openPolicy(context, LOCATION_TERMS_URL) },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "문의하기",
                        description = CONTACT_EMAIL,
                        onClick = { openContactEmail(context) },
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("앱 정보")
                SettingsGroup {
                    SettingsRow(
                        title = "앱 버전",
                        description = BuildConfig.VERSION_NAME,
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "데이터 출처",
                        description = "한국관광공사 TourAPI · 카카오맵",
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showSavedClearDialog) {
        AlertDialog(
            onDismissRequest = { showSavedClearDialog = false },
            title = { Text("저장 목록을 비울까요?") },
            text = { Text("이 기기에 저장한 장소가 모두 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearSaved()
                        showSavedClearDialog = false
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavedClearDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    if (showCacheClearDialog) {
        AlertDialog(
            onDismissRequest = { showCacheClearDialog = false },
            title = { Text("캐시를 지울까요?") },
            text = { Text("저장한 장소는 유지되고 임시 데이터만 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleared = clearAppCache(context)
                        showCacheClearDialog = false
                        Toast.makeText(
                            context,
                            if (cleared) "캐시를 정리했어요." else "캐시를 정리하지 못했어요.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                ) {
                    Text("지우기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCacheClearDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        color = TteumMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    title: String,
    description: String,
    titleColor: Color = TteumInk,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = titleColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(description, color = TteumMuted, fontSize = 13.sp)
        }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFB7BAC1),
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF0F1F3)),
    )
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
                .padding(padding)
                .statusBarsPadding(),
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
                        "평균 머무름 ${formatMinutes(place.stayMinutes)}",
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
    showHeart: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.selectable(
            selected = selected,
            role = Role.Checkbox,
            onClick = onClick,
        ),
        color = if (selected) TteumInk else Color(0xFFF4F5F7),
        shape = RoundedCornerShape(50),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE1E3E8)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                color = if (selected) Color.White else Color(0xFF596170),
                fontWeight = FontWeight.Bold,
            )
            if (showHeart) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = TteumRed,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
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
                        tint = if (isSaved) TteumRed else TteumMuted,
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
                    "평균 머무름 ${formatMinutes(place.stayMinutes)}",
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
    var bitmap by remember(imageUrl) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }
    LaunchedEffect(imageUrl) {
        val loadedBitmap = if (imageUrl.isBlank()) {
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
        bitmap = loadedBitmap
    }

    if (bitmap == null) {
        Box(
            modifier = modifier.background(TteumRedSoft),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = TteumRed,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text("틈새", color = TteumRed, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

private fun fallbackRouteSummary(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
): RouteSummary {
    val reference = recommendations.firstOrNull()
    val directMinutes = reference?.place?.let {
        (it.firstLegMinutes + it.secondLegMinutes - it.detourMinutes).coerceAtLeast(0)
    } ?: 0
    return RouteSummary(
        provider = "ESTIMATE",
        waypointCount = recommendations.size,
        totalDrivingMinutes = directMinutes + recommendations.sumOf { it.place.detourMinutes },
        totalDistanceMeters = reference?.place?.let {
            it.firstLegDistanceMeters + it.secondLegDistanceMeters
        } ?: 0,
        tollFareWon = 0,
        path = buildList {
            criteria.startCoordinates?.let(::add)
            recommendations.forEach { recommendation ->
                val latitude = recommendation.place.latitude
                val longitude = recommendation.place.longitude
                if (latitude != null && longitude != null) add(Coordinates(latitude, longitude))
            }
            criteria.endCoordinates?.let(::add)
        },
    )
}

private fun fallbackBaseRouteSummary(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
): RouteSummary {
    val reference = recommendations.firstOrNull()?.place
    val directMinutes = reference?.let {
        (it.firstLegMinutes + it.secondLegMinutes - it.detourMinutes).coerceAtLeast(0)
    } ?: 0
    return RouteSummary(
        provider = "ESTIMATE",
        waypointCount = 0,
        totalDrivingMinutes = directMinutes,
        totalDistanceMeters = 0,
        tollFareWon = 0,
        path = listOfNotNull(criteria.startCoordinates, criteria.endCoordinates),
    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationScreen(
    startName: String,
    endName: String,
    startLocation: LocationSearchResult?,
    endLocation: LocationSearchResult?,
    searchPlaces: suspend (String, Boolean) -> List<LocationSearchResult>,
    resolveCurrentAddress: suspend (Coordinates) -> String,
    onStartNameChange: (String) -> Unit,
    onEndNameChange: (String) -> Unit,
    onStartSelected: (LocationSearchResult) -> Unit,
    onEndSelected: (LocationSearchResult) -> Unit,
    isChecking: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible
    var isLocating by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    val locateCurrentPosition: () -> Unit = {
        isLocating = true
        requestCurrentLocation(
            context = context,
            onSuccess = { location ->
                onStartSelected(
                    LocationSearchResult(
                        id = "current-location",
                        name = "현재 위치",
                        address = "GPS로 확인한 위치",
                        coordinates = Coordinates(location.latitude, location.longitude),
                    ),
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
                    "현재 위치를 확인하지 못했어요. 잠시 후 다시 시도해 주세요.",
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
        } else if (deniedLocationPermissionNeedsSettings(permissions) { permission ->
                (context as? android.app.Activity)
                    ?.shouldShowRequestPermissionRationale(permission) == true
            }
        ) {
            showPermissionSettingsDialog = true
        } else {
            Toast.makeText(
                context,
                "현재 위치를 사용하려면 위치 권한을 허용해 주세요.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
    val useCurrentLocation = {
        if (hasLocationPermission(context)) {
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

    LaunchedEffect(startName, startLocation?.id) {
        if (shouldAutoLocateStart(startName, startLocation != null)) {
            useCurrentLocation()
        }
    }

    LaunchedEffect(startLocation?.id, startLocation?.name, startLocation?.coordinates) {
        val location = startLocation
        if (location?.id == "current-location" && location.name == "현재 위치") {
            val address = try {
                resolveCurrentAddress(location.coordinates)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                null
            }
            if (!address.isNullOrBlank()) {
                onStartSelected(location.copy(name = address, address = address))
            }
        }
    }

    val canContinue = startLocation != null && endLocation != null && !isChecking && !isLocating
    BackHandler(enabled = isImeVisible) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (!isImeVisible) {
                Surface(color = Color.White) {
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onNext()
                        },
                        enabled = canContinue,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            if (isChecking) "위치 확인 중..." else "다음",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp),
        ) {
            item {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "지금 어디로 가는 길인가요?",
                    fontSize = 31.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "틈새를 찾기 전 경로를 먼저 설정해야 해요",
                    color = TteumMuted,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(36.dp))
                Surface(
                    color = Color(0xFFF5F6F8),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column {
                        LocationSearchField(
                            value = startName,
                            onValueChange = onStartNameChange,
                            selected = startLocation,
                            label = "출발지",
                            labelBackground = TteumMuted,
                            searchPlaces = searchPlaces,
                            gangwonOnly = false,
                            onSelected = onStartSelected,
                            onUseCurrentLocation = useCurrentLocation,
                            isLocating = isLocating,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFDDE0E5),
                        )
                        LocationSearchField(
                            value = endName,
                            onValueChange = onEndNameChange,
                            selected = endLocation,
                            label = "목적지",
                            labelBackground = TteumInk,
                            searchPlaces = searchPlaces,
                            gangwonOnly = true,
                            onSelected = onEndSelected,
                            autoFocus = endLocation == null,
                        )
                    }
                }
            }
        }
    }

    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("위치 서비스를 켜 주세요") },
            text = { Text("현재 위치를 자동으로 설정하려면 휴대폰의 위치 서비스가 필요해요.") },
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


    if (showPermissionSettingsDialog) {
        LocationPermissionSettingsDialog(
            onDismiss = { showPermissionSettingsDialog = false },
            onOpenSettings = {
                showPermissionSettingsDialog = false
                openAppSettings(context)
            },
        )
    }
}

@Composable
private fun LocationPermissionSettingsDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("위치 권한을 켜 주세요") },
        text = {
            Text("위치 권한을 다시 묻지 않도록 설정했어요. 앱 설정에서 위치 권한을 허용해 주세요.")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("앱 설정 열기") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocationSearchField(
    value: String,
    selected: LocationSearchResult?,
    label: String,
    searchPlaces: suspend (String, Boolean) -> List<LocationSearchResult>,
    gangwonOnly: Boolean,
    onValueChange: (String) -> Unit,
    onSelected: (LocationSearchResult) -> Unit,
    labelBackground: Color,
    onUseCurrentLocation: (() -> Unit)? = null,
    isLocating: Boolean = false,
    autoFocus: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var results by remember { mutableStateOf(emptyList<LocationSearchResult>()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var searchFailed by remember { mutableStateOf(false) }
    var searchAttempt by remember { mutableStateOf(0) }
    var focusAfterClear by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val firstResultRequester = remember { BringIntoViewRequester() }
    val isCurrentLocation = selected?.id == "current-location"

    LaunchedEffect(autoFocus, selected?.id) {
        if (autoFocus && selected == null) {
            delay(250)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(selected) {
        if (selected == null && focusAfterClear) {
            focusRequester.requestFocus()
            focusAfterClear = false
        }
    }

    LaunchedEffect(value, selected?.id, searchAttempt) {
        results = emptyList()
        isLoading = false
        searchMessage = null
        searchFailed = false
        if (value.length < 2 || selected?.name == value || value == "현재 위치") return@LaunchedEffect
        delay(350)
        isLoading = true
        try {
            results = searchPlaces(value, gangwonOnly)
            if (results.isEmpty()) searchMessage = "검색 결과가 없어요. 장소명을 더 자세히 입력해 주세요."
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            searchFailed = true
            searchMessage = networkFailureMessage("장소 검색", error.message)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(results.firstOrNull()?.id) {
        if (results.isNotEmpty()) {
            delay(50)
            firstResultRequester.bringIntoView()
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = labelBackground,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (selected != null) {
                    Text(
                        value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusAfterClear = true
                                onValueChange("")
                            },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else if (value.isBlank()) {
                    Text("장소 검색", color = TteumMuted)
                }
                if (selected == null) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            },
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = TteumInk,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
            if (isLoading) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }
            if (onUseCurrentLocation != null) {
                Spacer(Modifier.width(10.dp))
                Surface(
                    modifier = Modifier.clickable(
                        enabled = !isLocating,
                        onClick = {
                            if (isCurrentLocation) onValueChange("") else onUseCurrentLocation()
                        },
                    ),
                    color = if (isCurrentLocation) TteumRed else TteumRedSoft,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 48.dp)
                            .heightIn(min = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (isLocating) "확인 중" else "현위치",
                            color = if (isCurrentLocation) Color.White else TteumRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        if (results.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                results.take(5).forEachIndexed { index, result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (index == 0) {
                                    Modifier.bringIntoViewRequester(firstResultRequester)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                onSelected(result)
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        } else if (searchMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    searchMessage.orEmpty(),
                    modifier = Modifier.weight(1f),
                    color = TteumMuted,
                    fontSize = 12.sp,
                )
                if (searchFailed) {
                    TextButton(onClick = { searchAttempt += 1 }) { Text("다시 시도") }
                }
            }
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
            label = "경로 따라 갈 장소",
            icon = Icons.Default.DirectionsCar,
            selected = mode == SearchMode.ON_THE_WAY,
            modifier = Modifier.weight(1f),
            onClick = { onModeChange(SearchMode.ON_THE_WAY) },
        )
        ModeTab(
            label = "근처에서 갈 장소",
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var isAdjustingTime by remember { mutableStateOf(false) }
    val typedDeadline = deadlineText.toIntOrNull()
    val isDeadlineValid = typedDeadline != null &&
        typedDeadline in MIN_DEADLINE_MINUTES..MAX_DEADLINE_MINUTES
    val isTimeValid = isDeadlineValid && buffer < (typedDeadline ?: 0)

    MapSheetScreen(
        onBack = onBack,
        bottomContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(0.32f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = TteumInk,
                    ),
                ) {
                    Text("이전", fontWeight = FontWeight.Bold)
                }
                Box(Modifier.weight(0.68f)) {
                    PrimaryButton(text = "다음", enabled = isTimeValid, onClick = onNext)
                }
            }
        },
    ) {
        Text(
            if (mode == SearchMode.ON_THE_WAY) {
                "경유에 사용할 여유시간은 얼마나 되나요?"
            } else {
                "다녀오는 데 사용할 시간은 얼마나 되나요?"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (mode == SearchMode.ON_THE_WAY) {
                "기본 경로 외에 추가로 사용할 수 있는 시간을 알려주세요."
            } else {
                "출발지로 돌아오기 전까지 사용할 시간을 알려주세요."
            },
            color = TteumMuted,
        )
        Spacer(Modifier.height(26.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val ticks = listOf(15 to "15분", 60 to "1시간", 120 to "2시간", 240 to "4시간", 360 to "6시간")
            ticks.forEachIndexed { index, (minutes, label) ->
                val markerModifier = when (index) {
                    0 -> Modifier.align(Alignment.CenterStart)
                    ticks.lastIndex -> Modifier.align(Alignment.CenterEnd)
                    else -> Modifier
                        .offset(
                            x = maxWidth * (
                                (minutes - MIN_DEADLINE_MINUTES).toFloat() /
                                    (SLIDER_MAX_DEADLINE_MINUTES - MIN_DEADLINE_MINUTES)
                                ),
                        )
                        .width(0.dp)
                }
                Box(modifier = markerModifier, contentAlignment = Alignment.Center) {
                    Text(label, modifier = Modifier.wrapContentSize(unbounded = true))
                }
            }
        }
        Spacer(Modifier.height(38.dp))
        Slider(
            value = deadline.coerceIn(MIN_DEADLINE_MINUTES, SLIDER_MAX_DEADLINE_MINUTES).toFloat(),
            onValueChange = { value ->
                isAdjustingTime = true
                onDeadlineChange(
                    ((value / 15f).roundToInt() * 15)
                        .coerceIn(MIN_DEADLINE_MINUTES, SLIDER_MAX_DEADLINE_MINUTES),
                )
            },
            onValueChangeFinished = { isAdjustingTime = false },
            valueRange = MIN_DEADLINE_MINUTES.toFloat()..SLIDER_MAX_DEADLINE_MINUTES.toFloat(),
            steps = 22,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                TimeSliderThumb(
                    value = deadline,
                    showValue = isAdjustingTime,
                )
            },
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = deadlineText,
            onValueChange = { input ->
                if (input.length <= 4 && input.all(Char::isDigit)) {
                    deadlineText = input
                    input.toIntOrNull()
                        ?.takeIf { it in MIN_DEADLINE_MINUTES..MAX_DEADLINE_MINUTES }
                        ?.let(onDeadlineChange)
                }
            },
            label = { Text(if (mode == SearchMode.ON_THE_WAY) "추가 여유시간" else "사용할 시간") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("분") },
            isError = deadlineText.isNotEmpty() && !isDeadlineValid,
            supportingText = if (deadlineText.isEmpty() || isDeadlineValid) {
                null
            } else {
                { Text("15분에서 24시간(1440분) 사이로 입력해 주세요.") }
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
            isEnabled = { it < deadline },
        )
        Text(
            "입력한 ${deadline}분 중 ${buffer}분은 도착 전 여유로 남기고, 나머지 시간 안에서 경유 이동과 머무름을 계산해요.",
            modifier = Modifier
                .fillMaxWidth()
                .background(TteumRedSoft, RoundedCornerShape(12.dp))
                .padding(14.dp),
            color = TteumRed,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun TimeSliderThumb(
    value: Int,
    showValue: Boolean,
) {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showValue) {
            Box(
                modifier = Modifier
                    .requiredSize(44.dp)
                    .background(TteumRed.copy(alpha = 0.14f), CircleShape),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-47).dp)
                    .wrapContentSize(unbounded = true),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = TteumRed,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        "${value}분",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Canvas(Modifier.width(10.dp).height(6.dp)) {
                    drawPath(
                        Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        },
                        color = TteumRed,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(TteumRed, CircleShape),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConditionsScreen(
    selectedIntents: Set<RecommendationIntent>,
    onIntentSelected: (RecommendationIntent) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { keyboardController?.hide() }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(color = Color.White) {
                Button(
                    onClick = onSearch,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("다음", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp),
        ) {
            item {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "좀 더 끌리는 장소가 있나요?",
                    fontSize = 31.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "1개 이상 골라주시면 더 만족스러운 틈새 장소가 될 거예요",
                    color = TteumMuted,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
                Spacer(Modifier.height(36.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RecommendationIntent.entries.forEach { intent ->
                        val selected = intent in selectedIntents
                        Surface(
                            modifier = Modifier.toggleable(
                                value = selected,
                                role = Role.Checkbox,
                                onValueChange = { onIntentSelected(intent) },
                            ),
                            color = if (selected) TteumRedSoft else Color(0xFFF5F6F8),
                            contentColor = if (selected) TteumRed else TteumMuted,
                            border = if (selected) BorderStroke(1.dp, TteumRed) else null,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                intent.label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
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
            errorMessage = networkFailureMessage("장소 추천", error.message)
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
                    color = TteumRedSoft,
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
                        "가는 길 주변에서\n들를 장소를 찾고 있어요.",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(16.dp))
                    listOf("기본 경로 확인", "실시간 경유 경로 비교", "머무름 시간 계산", "도착 전 여유 적용").forEach {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResultsScreen(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
    warning: String,
    baseRoute: RouteSummary?,
    corridorRadiusMeters: Int,
    currentLocationTarget: RequestedMapLocation?,
    onCurrentLocationTargetChange: (RequestedMapLocation?) -> Unit,
    summaryExpanded: Boolean,
    onSummaryExpandedChange: (Boolean) -> Unit,
    focusedPlaceId: String?,
    onFocusedPlaceIdChange: (String?) -> Unit,
    selectedIds: List<String>,
    onSelectedIdsChange: (List<String>) -> Unit,
    calculateRoute: suspend (List<PlaceCandidate>) -> RouteSummary,
    onBack: () -> Unit,
    onClearConditions: () -> Unit,
    onSearchOtherPlace: () -> Unit,
    onOpenRoute: (List<SafeRecommendation>) -> Unit,
    onSelect: (SafeRecommendation) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val directRoute = baseRoute ?: fallbackBaseRouteSummary(criteria, recommendations)
    var currentRoute by remember(baseRoute, criteria.startName, criteria.endName) {
        mutableStateOf(directRoute)
    }
    var isRecalculating by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var shouldCenterCurrentLocation by remember { mutableStateOf(false) }
    var showRouteInfo by remember { mutableStateOf(false) }
    val recommendationsById = recommendations.associateBy { it.place.id }
    val initialFocusedIndex = recommendations
        .indexOfFirst { it.place.id == focusedPlaceId }
        .coerceAtLeast(0)
    val cardListState = rememberLazyListState(initialFirstVisibleItemIndex = initialFocusedIndex)
    val cardSnapBehavior = rememberSnapFlingBehavior(cardListState)
    val focusedCardIndex by remember {
        derivedStateOf {
            val layout = cardListState.layoutInfo
            val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - viewportCenter)
            }?.index
        }
    }
    var hasObservedInitialCard by remember { mutableStateOf(false) }
    val selectedRecommendations = selectedIds.mapNotNull(recommendationsById::get)
    val totalMinutes = currentRoute.totalDrivingMinutes + selectedRecommendations.sumOf { it.place.stayMinutes }
    val isCurrentRouteWithinBudget = isRouteWithinExtraTimeBudget(
        baseDrivingMinutes = directRoute.totalDrivingMinutes,
        extraTimeMinutes = criteria.deadlineMinutesFromNow,
        selectedDrivingMinutes = currentRoute.totalDrivingMinutes,
        selectedStayMinutes = selectedRecommendations.sumOf { it.place.stayMinutes },
        safetyBufferMinutes = criteria.safetyBufferMinutes,
    )

    LaunchedEffect(recommendations, focusedPlaceId) {
        if (focusedPlaceId !in recommendationsById) {
            onFocusedPlaceIdChange(recommendations.firstOrNull()?.place?.id)
        }
    }

    LaunchedEffect(focusedCardIndex) {
        val index = focusedCardIndex ?: return@LaunchedEffect
        val nextFocusedPlaceId = recommendations.getOrNull(index)?.place?.id ?: return@LaunchedEffect
        if (nextFocusedPlaceId != focusedPlaceId) onFocusedPlaceIdChange(nextFocusedPlaceId)
        if (hasObservedInitialCard) {
            onSummaryExpandedChange(false)
        } else {
            hasObservedInitialCard = true
        }
    }

    LaunchedEffect(selectedIds, baseRoute) {
        if (selectedIds.isEmpty()) {
            currentRoute = directRoute
        } else if (currentRoute.waypointCount != selectedIds.size) {
            isRecalculating = true
            currentRoute = try {
                calculateRoute(selectedRecommendations.map { it.place })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fallbackRouteSummary(criteria, selectedRecommendations)
            }
            isRecalculating = false
        }
    }

    val locateCurrentPosition: () -> Unit = {
        isLocating = true
        requestCurrentLocation(
            context,
            onSuccess = {
                shouldCenterCurrentLocation = true
                onCurrentLocationTargetChange(
                    RequestedMapLocation(it.latitude, it.longitude, System.nanoTime()),
                )
                isLocating = false
            },
            onLocationDisabled = {
                shouldCenterCurrentLocation = false
                isLocating = false
                Toast.makeText(context, "휴대폰 위치 서비스를 켜 주세요.", Toast.LENGTH_SHORT).show()
            },
            onUnavailable = {
                shouldCenterCurrentLocation = false
                isLocating = false
                Toast.makeText(context, "현재 위치를 확인하지 못했어요.", Toast.LENGTH_SHORT).show()
            },
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { it }) {
            locateCurrentPosition()
        } else {
            shouldCenterCurrentLocation = false
            Toast.makeText(context, "현재 위치를 보려면 위치 권한을 허용해 주세요.", Toast.LENGTH_LONG).show()
        }
    }

    val toggleSelection: (SafeRecommendation) -> Unit = { recommendation ->
        onSummaryExpandedChange(false)
        if (isRecalculating) {
            Unit
        } else if (recommendation.place.id !in selectedIds && selectedIds.size >= MAX_KAKAO_WAYPOINTS) {
            Toast.makeText(context, "경유지는 최대 5곳까지 선택할 수 있어요.", Toast.LENGTH_SHORT).show()
        } else {
            val isRemoving = recommendation.place.id in selectedIds
            val nextIds = if (recommendation.place.id in selectedIds) {
                selectedIds - recommendation.place.id
            } else {
                selectedIds + recommendation.place.id
            }
            val nextPlaces = nextIds.mapNotNull { recommendationsById[it]?.place }
            isRecalculating = true
            scope.launch {
                val nextRoute = try {
                    calculateRoute(nextPlaces)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "실시간 경로를 다시 계산하지 못해 예상 경로를 보여드려요.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    fallbackRouteSummary(criteria, nextIds.mapNotNull(recommendationsById::get))
                }
                val nextStayMinutes = nextPlaces.sumOf { it.stayMinutes }
                if (!isRemoving && !isRouteWithinExtraTimeBudget(
                        baseDrivingMinutes = directRoute.totalDrivingMinutes,
                        extraTimeMinutes = criteria.deadlineMinutesFromNow,
                        selectedDrivingMinutes = nextRoute.totalDrivingMinutes,
                        selectedStayMinutes = nextStayMinutes,
                        safetyBufferMinutes = criteria.safetyBufferMinutes,
                    )
                ) {
                    Toast.makeText(
                        context,
                        "이 장소를 추가하면 추천 가능한 범위를 초과해요. 다른 장소를 골라주세요.",
                        Toast.LENGTH_LONG,
                    ).show()
                } else {
                    currentRoute = nextRoute
                    onSelectedIdsChange(nextIds)
                }
                isRecalculating = false
            }
        }
    }
    val focusCandidate: (String) -> Unit = focus@{ id ->
        val index = recommendations.indexOfFirst { it.place.id == id }
        if (index < 0) return@focus
        onFocusedPlaceIdChange(id)
        onSummaryExpandedChange(false)
        scope.launch { cardListState.scrollToItem(index) }
    }

    Scaffold(
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Button(
                    onClick = { onOpenRoute(selectedRecommendations) },
                    enabled = !isRecalculating && isCurrentRouteWithinBudget,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("이 경로로 카카오맵으로 안내받기", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val resultCardWidth = maxWidth * 0.84f
            val cardSidePadding = (maxWidth - resultCardWidth) / 2
            RouteMap(
                modifier = Modifier.fillMaxSize(),
                criteria = criteria,
                recommendation = null,
                routeSummary = currentRoute,
                candidates = recommendations,
                selectedIds = selectedIds,
                focusedPlaceId = focusedPlaceId,
                corridorRadiusMeters = corridorRadiusMeters,
                requestedLocation = currentLocationTarget,
                centerRequestedLocation = shouldCenterCurrentLocation,
                onMapInteraction = { onSummaryExpandedChange(false) },
                onCandidateClick = focusCandidate,
            )

            RouteSummaryCard(
                totalMinutes = totalMinutes,
                distanceMeters = currentRoute.totalDistanceMeters,
                tollFareWon = currentRoute.tollFareWon,
                isEstimate = currentRoute.provider == "ESTIMATE",
                waypointCount = selectedIds.size,
                isLoading = isRecalculating,
                isExpanded = summaryExpanded,
                onBack = onBack,
                onInfo = { showRouteInfo = true },
                onToggleExpanded = { onSummaryExpandedChange(!summaryExpanded) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 14.dp),
            )

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    RoundMapButton(
                        onClick = if (isLocating) null else {
                            {
                                onSummaryExpandedChange(false)
                                if (currentLocationTarget != null) {
                                    shouldCenterCurrentLocation = false
                                    onCurrentLocationTargetChange(null)
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
                        contentDescription = "현재 위치 표시",
                        selected = currentLocationTarget != null,
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
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                            }
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
                if (recommendations.isEmpty()) {
                    EmptyRouteResults(
                        warning = warning,
                        onClearConditions = onClearConditions,
                        onSearchOtherPlace = onSearchOtherPlace,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        state = cardListState,
                        flingBehavior = cardSnapBehavior,
                        contentPadding = PaddingValues(horizontal = cardSidePadding, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(recommendations, key = { _, item -> item.place.id }) { index, recommendation ->
                            RecommendationCard(
                                recommendation = recommendation,
                                position = index + 1,
                                total = recommendations.size,
                                isFocused = focusedPlaceId == recommendation.place.id,
                                selectedOrder = selectedIds.indexOf(recommendation.place.id)
                                    .takeIf { it >= 0 }
                                    ?.plus(1),
                                baseDistanceMeters = directRoute.totalDistanceMeters,
                                modifier = Modifier.width(resultCardWidth),
                                onToggle = { toggleSelection(recommendation) },
                                onClick = { onSelect(recommendation) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRouteInfo) {
        AlertDialog(
            onDismissRequest = { showRouteInfo = false },
            title = { Text("경유지 안내") },
            text = { Text("파란 핀을 누르면 해당 장소 카드를 볼 수 있어요. 카드의 ‘추가하기’로 최대 5곳을 고르면 시간·거리·통행료를 다시 계산해요.") },
            confirmButton = {
                TextButton(onClick = { showRouteInfo = false }) { Text("확인") }
            },
        )
    }
}

@Composable
private fun RecommendationCard(
    recommendation: SafeRecommendation,
    position: Int,
    total: Int,
    isFocused: Boolean,
    selectedOrder: Int?,
    baseDistanceMeters: Int,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    val additionalDistance = additionalDetourDistanceMeters(recommendation.place, baseDistanceMeters)
    val (visibleTags, hiddenTagCount) = compactTags(recommendation.place.tags, characterBudget = 22)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isFocused) BorderStroke(2.dp, RouteFocusBlue) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 7.dp else 4.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box {
                SavedPlaceImage(
                    imageUrl = recommendation.place.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TteumRedSoft),
                )
                RouteCardBadge(
                    text = "$position/$total",
                    selected = false,
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .sizeIn(minWidth = 56.dp, minHeight = 56.dp)
                        .semantics {
                            contentDescription = "${recommendation.place.name} 경유지"
                            stateDescription = selectedOrder?.let { "${it}번째로 추가됨" } ?: "추가되지 않음"
                        }
                        .toggleable(
                            value = selectedOrder != null,
                            role = Role.Checkbox,
                            onValueChange = { onToggle() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    RouteCardBadge(
                        text = selectedOrder?.let { "추가됨" } ?: "추가하기",
                        selected = selectedOrder != null,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    recommendation.place.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onClick) {
                    Text("상세보기", color = TteumMuted)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TteumMuted)
                }
            }
            Text(
                buildList {
                    additionalDistance?.let {
                        add("기본 경로보다 +${if (it == 0) "0m" else formatDistance(it)}")
                    }
                    add("추가 이동 약 ${recommendation.place.detourMinutes}분")
                    add(recommendation.place.category.label)
                }.joinToString(" · "),
                color = TteumMuted,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "평균 머무름 ${formatMinutes(recommendation.place.stayMinutes)} · 경로 총 +${formatMinutes(recommendation.place.detourMinutes + recommendation.place.stayMinutes)}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                visibleTags.forEach { tag ->
                    Surface(color = Color(0xFFF1F2F5), shape = RoundedCornerShape(50)) {
                        Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }
                if (hiddenTagCount > 0) {
                    Surface(color = Color(0xFFF1F2F5), shape = RoundedCornerShape(50)) {
                        Text("+$hiddenTagCount", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteCardBadge(text: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = if (selected) TteumRed else Color.White,
        contentColor = if (selected) Color.White else RouteFocusBlue,
        border = if (selected) null else BorderStroke(1.5.dp, RouteFocusBlue),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RouteSummaryCard(
    totalMinutes: Int,
    distanceMeters: Int,
    tollFareWon: Int,
    isEstimate: Boolean,
    waypointCount: Int,
    isLoading: Boolean,
    isExpanded: Boolean,
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        if (isExpanded) {
            Column {
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                    Text("총 소요시간", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("경유지 $waypointCount/$MAX_KAKAO_WAYPOINTS", color = TteumMuted, fontSize = 13.sp)
                    IconButton(onClick = onInfo) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "경유지 안내",
                            tint = TteumMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onToggleExpanded) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "경로 요약 접기",
                            modifier = Modifier.rotate(180f),
                        )
                    }
                }
                Column(Modifier.padding(start = 56.dp, end = 16.dp, bottom = 14.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                    } else {
                        Text(
                            if (isEstimate) "예상 ${formatMinutes(totalMinutes)}" else formatMinutes(totalMinutes),
                            color = TteumRed,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            if (isEstimate) {
                                "거리·통행료는 연결 후 확인"
                            } else {
                                "${formatDistance(distanceMeters)} · ${if (tollFareWon > 0) "통행료 ${"%,d".format(tollFareWon)}원" else "통행료 없음"}"
                            },
                            color = TteumMuted,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                    Spacer(Modifier.weight(1f))
                } else {
                    Text(
                        "${if (isEstimate) "예상 " else ""}${formatMinutes(totalMinutes)} · 경유지 $waypointCount/$MAX_KAKAO_WAYPOINTS",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onToggleExpanded) {
                    Icon(Icons.Default.ExpandMore, contentDescription = "경로 요약 펼치기")
                }
            }
        }
    }
}

@Composable
private fun EmptyRouteResults(
    warning: String,
    onClearConditions: () -> Unit,
    onSearchOtherPlace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("경로 주변에서 장소를 찾지 못했어요", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(warning.ifBlank { "관심 조건을 줄이거나 다른 목적지를 검색해 보세요." }, color = TteumMuted)
            OutlinedButton(onClick = onClearConditions, modifier = Modifier.fillMaxWidth()) { Text("관심 조건 해제하기") }
            TextButton(onClick = onSearchOtherPlace, modifier = Modifier.fillMaxWidth()) { Text("다른 목적지 검색하기") }
        }
    }
}

@Composable
private fun DetailScreen(
    criteria: SearchCriteria,
    recommendation: SafeRecommendation,
    warning: String,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    isOpeningRoute: Boolean,
    onOpenRoute: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", modifier = Modifier.size(30.dp))
                    }
                    Text(
                        "장소 상세",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onToggleSave, modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "저장 해제" else "저장",
                            tint = if (isSaved) TteumRed else TteumInk,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TteumRedSoft,
                        contentColor = TteumInk,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            "이 장소를 들르면 목적지까지 약 ${recommendation.place.detourMinutes}분 더 걸려요",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenRoute,
                        enabled = !isOpeningRoute,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isOpeningRoute) "경로 확인 중..." else "카카오맵에서 경유지로 안내",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                SavedPlaceImage(
                    imageUrl = recommendation.place.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .aspectRatio(1.58f),
                )
                Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Text(recommendation.place.category.label, color = TteumRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(recommendation.place.name, fontSize = 30.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailMetric(
                            icon = Icons.Default.Info,
                            text = "평균 머무름 ${formatMinutes(recommendation.place.stayMinutes)}",
                            modifier = Modifier.weight(0.9f),
                        )
                        DetailMetric(
                            icon = Icons.Default.DirectionsCar,
                            text = "${criteria.startName.ifBlank { "출발지" }}에서 ${recommendation.place.firstLegMinutes}분  |  목적지까지 +${recommendation.place.detourMinutes}분",
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                    Text("방문 정보", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6F8)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                VisitInfo("운영시간", recommendation.place.openingHours.ifBlank { "정보 확인 필요" })
                                VisitInfo("휴무일", recommendation.place.closedDays.ifBlank { "정보 확인 필요" })
                                VisitInfo("이용요금", "정보 확인 필요")
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                VisitInfo("주차", if ("주차 가능" in recommendation.place.tags) "가능" else "정보 확인 필요")
                                VisitInfo("활동", if (recommendation.place.category in setOf(PlaceCategory.ATTRACTION, PlaceCategory.LEISURE)) "야외 활동" else "실내·외 확인 필요")
                                VisitInfo("반려동물", if ("반려동물 동반" in recommendation.place.tags) "동반 가능" else "정보 확인 필요")
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("장소 소개", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        recommendation.place.reason.ifBlank { "강원도 여행 중 잠깐 들르기 좋은 장소예요." },
                        color = Color(0xFF4F535B),
                        lineHeight = 23.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openKakaoMap(context, recommendation.place.name) },
                        color = Color(0xFFF5F6F8),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = TteumMuted)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                recommendation.place.address.ifBlank { "주소 정보 확인 필요" },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TteumMuted)
                        }
                    }
                    if (warning.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Text(warning, color = TteumMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = Color(0xFFF5F6F8), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TteumRed, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun VisitInfo(label: String, value: String) {
    Column {
        Text(label, color = TteumMuted, fontSize = 12.sp)
        Text(value, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OperationStatusText(recommendation: SafeRecommendation) {
    Text(
        text = if (recommendation.operationStatus == OperationStatus.OPEN) {
            "도착 예상 시간에 운영 중"
        } else {
            "운영시간 확인 필요"
        },
        color = if (recommendation.operationStatus == OperationStatus.OPEN) TteumInk else TteumRed,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    )
}

@Composable
private fun SafetySummary(
    recommendation: SafeRecommendation,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TteumRedSoft),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "여유시간 반영 예상 · ${recommendation.marginMinutes}분 남아요",
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
        SafetyLevel.TIGHT -> TteumRedSoft
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapSheetScreen(
    onBack: () -> Unit,
    mapCoordinates: Coordinates? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetState = rememberStandardBottomSheetState(skipHiddenState = false)
        val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

        LaunchedEffect(sheetState.currentValue) {
            if (sheetState.currentValue == SheetValue.Hidden) onBack()
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = maxHeight * 0.54f,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetContainerColor = Color.White,
            sheetShadowElevation = 10.dp,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetContent = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.90f)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = if (bottomContent == null) 20.dp else 112.dp),
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
            },
        ) {
            val requestedLocation = mapCoordinates?.let { coordinates ->
                RequestedMapLocation(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    requestId = coordinates.hashCode().toLong(),
                )
            }
            MapBackground(
                modifier = Modifier.fillMaxSize(),
                requestedLocation = requestedLocation,
            )
        }

        if (bottomContent != null && sheetState.currentValue != SheetValue.Hidden) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                ) {
                    bottomContent()
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
    contentDescription: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    foreground: Color = TteumInk,
    border: BorderStroke? = null,
) {
    Surface(
        modifier = modifier
            .size(52.dp)
            .semantics {
                this.contentDescription = contentDescription
                stateDescription = when {
                    onClick == null -> "위치 확인 중"
                    selected -> "활성화"
                    else -> "비활성화"
                }
            }
            .toggleable(
                value = selected,
                enabled = onClick != null,
                role = Role.Switch,
                onValueChange = { onClick?.invoke() },
            ),
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
    isEnabled: (T) -> Boolean = { true },
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(choices) { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                enabled = isEnabled(value),
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
            containerColor = if (selected) TteumRedSoft else Color.Transparent,
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
        drawCircle(TteumRed.copy(alpha = 0.33f), radius = 28f, center = Offset(size.width * 0.5f, size.height * 0.46f))
    }
}

@Composable
private fun RouteMap(
    modifier: Modifier = Modifier,
    criteria: SearchCriteria,
    recommendation: SafeRecommendation?,
    routeSummary: RouteSummary? = null,
    candidates: List<SafeRecommendation> = emptyList(),
    selectedIds: List<String> = emptyList(),
    focusedPlaceId: String? = null,
    corridorRadiusMeters: Int = 0,
    requestedLocation: RequestedMapLocation? = null,
    centerRequestedLocation: Boolean = false,
    onMapInteraction: () -> Unit = {},
    onCandidateClick: (String) -> Unit = {},
) {
    val waypoint = recommendation?.place?.latitude?.let { latitude ->
        recommendation.place.longitude?.let { longitude -> Coordinates(latitude, longitude) }
    }
    val fallbackPoints = listOfNotNull(
        criteria.startCoordinates,
        waypoint,
        criteria.endCoordinates,
    )
    val routeStops = buildList {
        criteria.startCoordinates?.let { add("출발지" to it) }
        if (criteria.mode == SearchMode.ON_THE_WAY) {
            criteria.endCoordinates?.let { add("목적지" to it) }
        }
    }
    val mapCandidates = candidates.mapNotNull { candidate ->
        candidate.place.latitude?.let { latitude ->
            candidate.place.longitude?.let { longitude ->
                MapCandidate(
                    id = candidate.place.id,
                    coordinates = Coordinates(latitude, longitude),
                    category = candidate.place.category,
                    selectedOrder = selectedIds.indexOf(candidate.place.id).takeIf { it >= 0 }?.plus(1),
                    isFocused = candidate.place.id == focusedPlaceId,
                )
            }
        }
    }
    val routePoints = routeSummary?.path?.ifEmpty { fallbackPoints }
        ?: recommendation?.routePoints?.ifEmpty { fallbackPoints }
        ?: fallbackPoints
    KakaoMapSurface(
        modifier = modifier,
        latitude = waypoint?.latitude ?: criteria.endCoordinates?.latitude ?: 37.7645,
        longitude = waypoint?.longitude ?: criteria.endCoordinates?.longitude ?: 128.8996,
        zoomLevel = 15,
        requestedLocation = requestedLocation,
        centerRequestedLocation = centerRequestedLocation,
        routePoints = routePoints,
        routeStops = routeStops,
        candidateMarkers = mapCandidates,
        corridorPoints = routePoints,
        corridorRadiusMeters = corridorRadiusMeters.takeIf { it > 0 }
            ?: (criteria.deadlineMinutesFromNow * 20).coerceIn(800, 8_000),
        onMapInteraction = onMapInteraction,
        onCandidateClick = onCandidateClick,
    )
}

@Composable
private fun KakaoMapSurface(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoomLevel: Int,
    requestedLocation: RequestedMapLocation? = null,
    centerRequestedLocation: Boolean = true,
    routePoints: List<Coordinates> = emptyList(),
    routeStops: List<Pair<String, Coordinates>> = emptyList(),
    candidateMarkers: List<MapCandidate> = emptyList(),
    corridorPoints: List<Coordinates> = emptyList(),
    corridorRadiusMeters: Int = 0,
    onMapInteraction: () -> Unit = {},
    onCandidateClick: (String) -> Unit = {},
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
    var mapAttempt by remember { mutableStateOf(0) }
    val mapView = remember(mapAttempt) { MapView(context) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLocationLabel by remember { mutableStateOf<Label?>(null) }
    val currentOnMapInteraction by rememberUpdatedState(onMapInteraction)
    val currentOnCandidateClick by rememberUpdatedState(onCandidateClick)
    val normalCandidateMarkers = candidateMarkers
        .filter { it.selectedOrder == null }
        .map { it.copy(isFocused = false) }
    val highlightedCandidateMarkers = candidateMarkers.filter {
        it.isFocused || it.selectedOrder != null
    }

    DisposableEffect(mapView, lifecycleOwner) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(error: Exception) {
                    mapError = networkFailureMessage("지도 불러오기", error.message)
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

    LaunchedEffect(kakaoMap, requestedLocation?.requestId, centerRequestedLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        val target = requestedLocation
        if (target == null) {
            currentLocationLabel?.remove()
            currentLocationLabel = null
            return@LaunchedEffect
        }
        val position = LatLng.from(target.latitude, target.longitude)

        if (centerRequestedLocation) {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(position, 16),
                CameraAnimation.from(500),
            )
        }

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

    DisposableEffect(kakaoMap) {
        val map = kakaoMap
        if (map == null) {
            onDispose { }
        } else {
            map.setOnMapClickListener { _, _, _, _ -> currentOnMapInteraction() }
            map.setOnCameraMoveStartListener { _, gesture ->
                if (gesture != GestureType.Unknown) currentOnMapInteraction()
            }
            map.setOnLabelClickListener { _, _, label ->
                (label.tag as? String)?.let(currentOnCandidateClick)
                label.tag is String
            }
            onDispose {
                map.setOnMapClickListener(null)
                map.setOnCameraMoveStartListener(null)
                map.setOnLabelClickListener(null)
            }
        }
    }

    DisposableEffect(
        kakaoMap,
        routePoints,
        routeStops,
        corridorPoints,
        corridorRadiusMeters,
    ) {
        val map = kakaoMap
        if (map == null || routePoints.size < 2) {
            onDispose { }
        } else {
            val points = routePoints.map { LatLng.from(it.latitude, it.longitude) }
            val routeLine = map.routeLineManager?.layer?.addRouteLine(
                RouteLineOptions.from(
                    RouteLineSegment.from(
                        points,
                        RouteLineStyle.from(8f, TteumRed.copy(alpha = 0.80f).toArgb(), 2f, Color.White.toArgb()),
                    ),
                ),
            )
            val labels = routeStops.mapIndexedNotNull { index, (name, coordinates) ->
                map.labelManager?.layer?.addLabel(
                    LabelOptions.from(
                        "route-stop-$index",
                        LatLng.from(coordinates.latitude, coordinates.longitude),
                    ).setStyles(createRouteStopBitmap(context, name)),
                )
            }
            val corridorPolygons = if (corridorRadiusMeters > 0) {
                val step = (corridorPoints.size / 10).coerceAtLeast(1)
                corridorPoints.filterIndexed { index, _ ->
                    index == 0 || index == corridorPoints.lastIndex || index % step == 0
                }.mapNotNull { point ->
                    map.shapeManager?.layer?.addPolygon(
                        PolygonOptions.from(
                            DotPoints.fromCircle(
                                LatLng.from(point.latitude, point.longitude),
                                corridorRadiusMeters.toFloat(),
                            ),
                        ).setStylesSet(
                            PolygonStylesSet.from(
                                PolygonStyles.from(TteumRed.copy(alpha = 0.08f).toArgb()),
                            ),
                        ),
                    )
                }
            } else {
                emptyList()
            }

            onDispose {
                routeLine?.remove()
                labels.forEach(Label::remove)
                corridorPolygons.forEach { it.remove() }
            }
        }
    }

    DisposableEffect(kakaoMap, normalCandidateMarkers) {
        val map = kakaoMap
        val labelManager = map?.labelManager
        if (map == null || labelManager == null) {
            onDispose { }
        } else {
            val normalLayer = labelManager.addLayer(
                LabelLayerOptions.from("tteumsae-route-candidates")
                    .setCompetitionType(CompetitionType.Same)
                    .setCompetitionUnit(CompetitionUnit.IconAndText)
                    .setOrderingType(OrderingType.Rank)
                    .setZOrder(5_000)
                    .setClickable(true),
            )
            val candidateLabels = normalCandidateMarkers.mapIndexedNotNull { index, candidate ->
                normalLayer?.addLabel(
                    LabelOptions.from(
                        "candidate-${candidate.id}",
                        LatLng.from(candidate.coordinates.latitude, candidate.coordinates.longitude),
                    )
                        .setStyles(
                            createCandidateMarkerBitmap(
                                context,
                                candidate.category,
                                selectedOrder = null,
                                isFocused = false,
                            ),
                        )
                        .setRank((normalCandidateMarkers.size - index).toLong())
                        .setTag(candidate.id)
                        .setClickable(true),
                )
            }

            onDispose {
                candidateLabels.forEach(Label::remove)
                normalLayer?.let(labelManager::remove)
            }
        }
    }

    DisposableEffect(kakaoMap, highlightedCandidateMarkers) {
        val map = kakaoMap
        val labelManager = map?.labelManager
        if (map == null || labelManager == null) {
            onDispose { }
        } else {
            val highlightedLayer = labelManager.addLayer(
                LabelLayerOptions.from("tteumsae-route-highlights")
                    .setCompetitionType(CompetitionType.None)
                    .setOrderingType(OrderingType.Rank)
                    .setZOrder(5_100)
                    .setClickable(true),
            )
            val candidateLabels = highlightedCandidateMarkers.mapNotNull { candidate ->
                val rank = if (candidate.isFocused) {
                    30_000L
                } else {
                    20_000L - (candidate.selectedOrder ?: 0)
                }
                highlightedLayer?.addLabel(
                    LabelOptions.from(
                        "highlight-${candidate.id}",
                        LatLng.from(candidate.coordinates.latitude, candidate.coordinates.longitude),
                    )
                        .setStyles(
                            createCandidateMarkerBitmap(
                                context,
                                candidate.category,
                                candidate.selectedOrder,
                                candidate.isFocused,
                            ),
                        )
                        .setRank(rank)
                        .setTag(candidate.id)
                        .setClickable(true),
                )
            }

            onDispose {
                candidateLabels.forEach(Label::remove)
                highlightedLayer?.let(labelManager::remove)
            }
        }
    }

    LaunchedEffect(kakaoMap, routePoints, requestedLocation?.requestId, centerRequestedLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        if ((!centerRequestedLocation || requestedLocation == null) && routePoints.size >= 2) {
            val points = routePoints.map { LatLng.from(it.latitude, it.longitude) }
            map.moveCamera(
                CameraUpdateFactory.fitMapPoints(points.toTypedArray(), 80),
                CameraAnimation.from(500),
            )
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(message, color = TteumMuted, fontSize = 13.sp)
                    TextButton(
                        onClick = {
                            mapError = null
                            mapAttempt += 1
                        },
                    ) {
                        Text("지도 다시 시도")
                    }
                }
            }
        }
    }
}

private fun createRouteStopBitmap(context: Context, text: String): Bitmap {
    val density = context.resources.displayMetrics.density
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 13f * density
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val width = (textPaint.measureText(text) + 24f * density).roundToInt()
    val height = (34f * density).roundToInt()
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = android.graphics.Canvas(bitmap)
        val background = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = if (text.startsWith("출발")) 0xFFF0647B.toInt() else TteumRed.toArgb()
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, background)
        val baseline = height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(text, width / 2f, baseline, textPaint)
    }
}

private fun createCandidateMarkerBitmap(
    context: Context,
    category: PlaceCategory,
    selectedOrder: Int?,
    isFocused: Boolean,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = 48f * density
    return Bitmap.createBitmap(size.roundToInt(), size.roundToInt(), Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = android.graphics.Canvas(bitmap)
        val center = size / 2f
        val isSelected = selectedOrder != null
        val markerColor = if (isSelected) TteumRed else RouteFocusBlue
        if (isFocused) {
            val halo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = RouteFocusBlue.copy(alpha = 0.18f).toArgb()
            }
            canvas.drawCircle(center, center, 22f * density, halo)
        }
        val background = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        val border = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = when {
                isSelected -> TteumRed.toArgb()
                isFocused -> RouteFocusBlue.toArgb()
                else -> 0xFFD4D7DC.toInt()
            }
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = (if (isFocused || isSelected) 2.5f else 1.25f) * density
        }
        val outerRadius = (if (isFocused || isSelected) 19f else 15f) * density
        canvas.drawCircle(center, center, outerRadius, background)
        canvas.drawCircle(center, center, outerRadius, border)

        val iconBackground = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = markerColor.toArgb()
        }
        canvas.drawCircle(center, center, (if (isFocused || isSelected) 13f else 10.5f) * density, iconBackground)
        drawCategoryMarkerIcon(canvas, category, center, center, density)

        selectedOrder?.let { order ->
            val badgeCenter = 38f * density
            canvas.drawCircle(badgeCenter, 10f * density, 9f * density, background)
            canvas.drawCircle(badgeCenter, 10f * density, 8f * density, iconBackground)
            val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 10f * density
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val baseline = 10f * density - (textPaint.ascent() + textPaint.descent()) / 2f
            canvas.drawText(order.toString(), badgeCenter, baseline, textPaint)
        }
    }
}

private fun drawCategoryMarkerIcon(
    canvas: android.graphics.Canvas,
    category: PlaceCategory,
    centerX: Float,
    centerY: Float,
    density: Float,
) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1.7f * density
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }
    fun line(x1: Float, y1: Float, x2: Float, y2: Float) = canvas.drawLine(
        centerX + x1 * density,
        centerY + y1 * density,
        centerX + x2 * density,
        centerY + y2 * density,
        paint,
    )
    when (category) {
        PlaceCategory.RESTAURANT -> {
            line(-4f, -6f, -4f, 6f)
            line(-7f, -6f, -7f, -1f)
            line(-1f, -6f, -1f, -1f)
            line(-7f, -1f, -1f, -1f)
            line(4f, -6f, 4f, 6f)
            line(4f, -6f, 7f, -2f)
        }
        PlaceCategory.CAFE -> {
            canvas.drawRoundRect(
                android.graphics.RectF(centerX - 6f * density, centerY - 4f * density, centerX + 4f * density, centerY + 4f * density),
                1.5f * density,
                1.5f * density,
                paint,
            )
            canvas.drawArc(
                android.graphics.RectF(centerX + 2f * density, centerY - 2.5f * density, centerX + 8f * density, centerY + 3f * density),
                -80f,
                160f,
                false,
                paint,
            )
            line(-7f, 6f, 7f, 6f)
        }
        PlaceCategory.ATTRACTION -> {
            line(-7f, 5f, -2f, -4f)
            line(-2f, -4f, 1f, 1f)
            line(1f, 1f, 4f, -3f)
            line(4f, -3f, 8f, 5f)
            line(-7f, 5f, 8f, 5f)
        }
        PlaceCategory.CULTURE -> {
            line(-7f, -3f, 0f, -7f)
            line(0f, -7f, 7f, -3f)
            line(-7f, -3f, 7f, -3f)
            listOf(-5f, 0f, 5f).forEach { x -> line(x, -2f, x, 5f) }
            line(-8f, 6f, 8f, 6f)
        }
        PlaceCategory.SHOPPING -> {
            canvas.drawRoundRect(
                android.graphics.RectF(centerX - 6f * density, centerY - 3f * density, centerX + 6f * density, centerY + 7f * density),
                1.5f * density,
                1.5f * density,
                paint,
            )
            canvas.drawArc(
                android.graphics.RectF(centerX - 3f * density, centerY - 7f * density, centerX + 3f * density, centerY - 1f * density),
                180f,
                180f,
                false,
                paint,
            )
        }
        PlaceCategory.FESTIVAL -> {
            repeat(8) { index ->
                val angle = Math.toRadians(index * 45.0)
                line(
                    (kotlin.math.cos(angle) * 3f).toFloat(),
                    (kotlin.math.sin(angle) * 3f).toFloat(),
                    (kotlin.math.cos(angle) * 7f).toFloat(),
                    (kotlin.math.sin(angle) * 7f).toFloat(),
                )
            }
            canvas.drawCircle(centerX, centerY, 2f * density, paint)
        }
        PlaceCategory.LEISURE -> {
            canvas.drawCircle(centerX, centerY, 7f * density, paint)
            line(-6f, -3f, 6f, 3f)
            line(-6f, 3f, 6f, -3f)
        }
    }
}

internal fun shouldShowHomeIntro(hiddenDate: String?, today: String): Boolean =
    hiddenDate != today

private fun shouldShowHomeIntro(context: Context): Boolean {
    val hiddenDate = context
        .getSharedPreferences(HOME_INTRO_PREFERENCES, Context.MODE_PRIVATE)
        .getString(HOME_INTRO_HIDDEN_DATE, null)
    return shouldShowHomeIntro(hiddenDate, LocalDate.now().toString())
}

private fun hideHomeIntroForToday(context: Context) {
    context
        .getSharedPreferences(HOME_INTRO_PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putString(HOME_INTRO_HIDDEN_DATE, LocalDate.now().toString())
        .apply()
}
