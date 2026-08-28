package com.tteumsae.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.tteumsae.app.TteumsaeApplication
import com.tteumsae.app.data.TteumsaeApi
import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.OperationStatus
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SafetyLevel
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.domain.SearchMode
import com.tteumsae.app.domain.TransportMode
import com.tteumsae.app.domain.route.RouteLocation
import com.tteumsae.app.domain.route.SAFETY_BUFFER_MINUTES
import com.tteumsae.app.domain.route.remainingWholeMinutes
import com.tteumsae.app.platform.CONTACT_EMAIL
import com.tteumsae.app.platform.LOCATION_TERMS_URL
import com.tteumsae.app.platform.PRIVACY_POLICY_URL
import com.tteumsae.app.platform.clearAppCache
import com.tteumsae.app.platform.buildKakaoMapMultiRouteUrl
import com.tteumsae.app.platform.isKakaoMapAvailable
import com.tteumsae.app.platform.openAppSettings
import com.tteumsae.app.platform.openContactEmail
import com.tteumsae.app.platform.openKakaoMap
import com.tteumsae.app.platform.openKakaoMapHome
import com.tteumsae.app.platform.openKakaoMapInstallPage
import com.tteumsae.app.platform.openKakaoMapMultiRoute
import com.tteumsae.app.platform.openKakaoMapRoute
import com.tteumsae.app.platform.openPolicy
import com.tteumsae.app.ui.common.formatDistance
import com.tteumsae.app.ui.navigation.AppDestination
import com.tteumsae.app.ui.navigation.MainTab
import com.tteumsae.app.ui.navigation.previousDestination
import com.tteumsae.app.ui.navigation.safeRestoredDestination
import com.tteumsae.app.ui.route.RouteFlowViewModel
import com.tteumsae.app.ui.route.RouteLocationScreen
import com.tteumsae.app.ui.route.RouteResultsScreen
import com.tteumsae.app.ui.route.RouteStage
import com.tteumsae.app.ui.saved.SavedPlacesScreen
import com.tteumsae.app.ui.saved.SavedPlaceImage
import com.tteumsae.app.ui.settings.SettingsScreen
import com.tteumsae.app.ui.account.AccountViewModel
import com.tteumsae.app.ui.account.AccountViewModelFactory
import com.tteumsae.app.ui.account.ProfileEditScreen
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import com.tteumsae.app.reminder.ActiveTrip
import com.tteumsae.app.reminder.activeTripExpiryEpochMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

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

private val RouteFocusBlue = Color(0xFF2F6FE4)

private data class MapCandidate(
    val id: String,
    val coordinates: Coordinates,
    val detourMinutes: Int,
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

@Composable
fun TteumsaeApp() {
    val context = LocalContext.current
    val api = remember { TteumsaeApi() }
    val application = context.applicationContext as TteumsaeApplication
    val routeViewModel: RouteFlowViewModel = viewModel(
        factory = remember(application) {
            RouteFlowViewModel.factory(application.container.routeGateway)
        },
    )
    val routeState by routeViewModel.uiState.collectAsStateWithLifecycle()
    var reminderEnabledPlaceId by rememberSaveable {
        mutableStateOf(application.container.activeTripStore.loadValid()?.stopId)
    }
    var pendingReminderTrip by remember { mutableStateOf<ActiveTrip?>(null) }
    var pendingReminderPlaceId by remember { mutableStateOf<String?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val trip = pendingReminderTrip
        val placeId = pendingReminderPlaceId
        if (granted && trip != null && placeId != null) {
            application.container.activeTripStore.save(trip)
            application.container.departureReminderScheduler.schedule(trip)
            reminderEnabledPlaceId = placeId
        } else if (!granted) {
            Toast.makeText(
                context,
                "알림 권한 없이도 길 안내는 그대로 이용할 수 있어요.",
                Toast.LENGTH_LONG,
            ).show()
        }
        pendingReminderTrip = null
        pendingReminderPlaceId = null
    }
    val accountViewModel: AccountViewModel = viewModel(
        factory = remember(application) {
            AccountViewModelFactory(
                authRepository = application.container.authRepository,
                profileRepository = application.container.profileRepository,
                deletionClient = application.container.accountDeletionClient,
            )
        },
    )
    val accountState by accountViewModel.state.collectAsStateWithLifecycle()
    val savedPlacesRepository = remember(application) {
        application.container.savedPlacesRepository
    }
    val savedPlacesFlow = remember(savedPlacesRepository) {
        savedPlacesRepository.observeSaved()
    }
    val savedPlaces by savedPlacesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var screen by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var selected by remember { mutableStateOf<SafeRecommendation?>(null) }
    var locationChecking by remember { mutableStateOf(false) }
    var currentLocationTarget by remember { mutableStateOf<RequestedMapLocation?>(null) }
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

    val hasRouteLocations = routeState.input.start != null &&
        routeState.input.destination != null
    val safeScreen = if (
        screen == AppDestination.LOADING && routeState.stage == RouteStage.LOADING
    ) {
        AppDestination.LOADING
    } else {
        safeRestoredDestination(
            current = screen,
            hasLocations = hasRouteLocations,
            hasResults = routeState.stage == RouteStage.RESULTS,
            hasDetail = selected != null,
        )
    }
    LaunchedEffect(safeScreen) {
        if (screen != safeScreen) screen = safeScreen
    }
    LaunchedEffect(routeState.stage) {
        if (
            screen in setOf(
                AppDestination.LOCATION,
                AppDestination.LOADING,
                AppDestination.RESULTS,
            )
        ) {
            screen = when (routeState.stage) {
                RouteStage.LOCATION -> AppDestination.LOCATION
                RouteStage.LOADING -> AppDestination.LOADING
                RouteStage.RESULTS -> AppDestination.RESULTS
            }
        }
    }

    BackHandler(enabled = screen != AppDestination.HOME) {
        if (screen == AppDestination.LOADING || screen == AppDestination.RESULTS) {
            routeViewModel.startNewSearch()
            screen = AppDestination.LOCATION
        } else {
            screen = previousDestination(screen)
        }
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
        mode = SearchMode.ON_THE_WAY,
        startName = routeState.input.start?.name.orEmpty(),
        endName = routeState.input.destination?.name.orEmpty(),
        deadlineMinutesFromNow = routeState.input.arrivalDeadlineEpochMillis?.let {
            remainingWholeMinutes(it, System.currentTimeMillis()).coerceAtLeast(0)
        } ?: 0,
        safetyBufferMinutes = SAFETY_BUFFER_MINUTES,
        transportMode = TransportMode.CAR,
        categories = routeState.input.categories,
        startCoordinates = routeState.input.start?.coordinates,
        endCoordinates = routeState.input.destination?.coordinates,
        arrivalDeadlineEpochMillis = routeState.input.arrivalDeadlineEpochMillis,
    )
    val openRoute: (List<SafeRecommendation>) -> Unit = { routeRecommendations ->
        val resolved = criteria
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
                routeViewModel.startNewSearch()
                routeViewModel.updateStart(
                    coordinates?.let { RouteLocation("현재 위치", it) },
                )
                routeViewModel.updateDestination(null)
                routeViewModel.updateDeadline(null)
                routeViewModel.updateFilters(emptySet())
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
            regions = gangwonRegionCodes.keys.toList(),
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
                appScope.launch {
                    savedPlacesRepository.toggleGuest(place, System.currentTimeMillis())
                }
            },
            onRestore = { entry ->
                appScope.launch { savedPlacesRepository.restoreGuest(entry) }
            },
            onOpenMap = { place -> openKakaoMap(context, place.name) },
            onTabSelected = { tab ->
                screen = when (tab) {
                    MainTab.EXPLORE -> AppDestination.HOME
                    MainTab.SAVED -> AppDestination.SAVED
                    MainTab.SETTINGS -> AppDestination.SETTINGS
                }
            },
        )

        AppDestination.SETTINGS -> {
            val lifecycleOwner = LocalLifecycleOwner.current
            var locationPermissionGranted by remember {
                mutableStateOf(hasLocationPermission(context))
            }
            var kakaoMapAvailable by remember {
                mutableStateOf(isKakaoMapAvailable(context))
            }

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

            SettingsScreen(
                accountState = accountState,
                savedCount = savedPlaces.size,
                locationPermissionGranted = locationPermissionGranted,
                kakaoMapAvailable = kakaoMapAvailable,
                appVersion = BuildConfig.VERSION_NAME,
                contactEmail = CONTACT_EMAIL,
                privacyPolicyAvailable = PRIVACY_POLICY_URL.isNotBlank(),
                locationTermsAvailable = LOCATION_TERMS_URL.isNotBlank(),
                onOpenLogin = accountViewModel::openLogin,
                onDismissLogin = accountViewModel::dismissLogin,
                onLogin = accountViewModel::signIn,
                onOpenProfile = {
                    if (accountState.profile != null) screen = AppDestination.PROFILE
                },
                onSignOut = accountViewModel::signOut,
                onRequestAccountDeletion = accountViewModel::requestDeletion,
                onConfirmDeletionConsequences = accountViewModel::confirmDeletionConsequences,
                onRequireReauthentication = accountViewModel::requireReauthentication,
                onReauthenticateForDeletion = accountViewModel::reauthenticateForDeletion,
                onCancelDeletion = accountViewModel::cancelDeletion,
                onOpenLocationSettings = { openAppSettings(context) },
                onOpenKakaoMap = {
                    if (kakaoMapAvailable) {
                        openKakaoMapHome(context)
                    } else {
                        openKakaoMapInstallPage(context)
                    }
                },
                onClearCache = { clearAppCache(context) },
                onClearSaved = {
                    appScope.launch { savedPlacesRepository.clearGuest() }
                },
                onOpenPrivacyPolicy = { openPolicy(context, PRIVACY_POLICY_URL) },
                onOpenLocationTerms = { openPolicy(context, LOCATION_TERMS_URL) },
                onContact = { openContactEmail(context) },
                onShowMessage = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                onTabSelected = { tab ->
                    screen = when (tab) {
                        MainTab.EXPLORE -> AppDestination.HOME
                        MainTab.SAVED -> AppDestination.SAVED
                        MainTab.SETTINGS -> AppDestination.SETTINGS
                    }
                },
            )
        }

        AppDestination.PROFILE -> accountState.profile?.let { profile ->
            ProfileEditScreen(
                profile = profile,
                isLoading = accountState.isLoading,
                errorMessage = accountState.errorMessage,
                onBack = { screen = AppDestination.SETTINGS },
                onSave = accountViewModel::saveProfile,
            )
        } ?: run {
            LaunchedEffect(Unit) { screen = AppDestination.SETTINGS }
        }

        AppDestination.LOCATION -> RouteLocationScreen(
            input = routeState.input,
            errorMessage = routeState.errorMessage,
            searchPlaces = api::searchPlaces,
            resolveCurrentAddress = api::regionAddress,
            onStartSelected = {
                routeViewModel.updateStart(it)
                currentLocationTarget = it?.let { location ->
                    RequestedMapLocation(
                        latitude = location.coordinates.latitude,
                        longitude = location.coordinates.longitude,
                        requestId = System.nanoTime(),
                    )
                }
            },
            onDestinationSelected = routeViewModel::updateDestination,
            onDeadlineSelected = routeViewModel::updateDeadline,
            onFiltersChanged = routeViewModel::updateFilters,
            isChecking = locationChecking,
            onBack = { screen = AppDestination.HOME },
            onNext = {
                if (!locationChecking) {
                    locationChecking = true
                    appScope.launch {
                        try {
                            val destination = requireNotNull(routeState.input.destination)
                            if (!api.isGangwon(destination.coordinates)) {
                                Toast.makeText(
                                    context,
                                    "목적지가 강원도 밖이라 이동 중 들를 장소를 추천할 수 없어요.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                routeViewModel.search()
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

        AppDestination.LOADING -> LoadingScreen(
            onBack = {
                routeViewModel.startNewSearch()
                screen = AppDestination.LOCATION
            },
        )

        AppDestination.RESULTS -> RouteResultsScreen(
            criteria = criteria,
            recommendations = routeState.recommendations,
            baseRoute = routeState.baseRoute,
            corridorRadiusMeters = routeState.corridorRadiusMeters,
            selectedPlaceId = routeState.selectedPlaceId,
            warning = routeState.warning,
            isRefreshing = routeState.isRefreshing,
            reminderEnabled = reminderEnabledPlaceId == routeState.selectedPlaceId,
            onSelectPlace = { placeId ->
                if (reminderEnabledPlaceId != null && reminderEnabledPlaceId != placeId) {
                    application.container.departureReminderScheduler.cancel()
                    application.container.activeTripStore.clear()
                    reminderEnabledPlaceId = null
                }
                routeViewModel.selectPlace(placeId)
            },
            onClearSelection = {
                application.container.departureReminderScheduler.cancel()
                application.container.activeTripStore.clear()
                reminderEnabledPlaceId = null
                routeViewModel.clearSelection()
            },
            onRefresh = routeViewModel::refresh,
            onReminderChanged = reminder@{ recommendation, enabled ->
                if (!enabled) {
                    application.container.departureReminderScheduler.cancel()
                    application.container.activeTripStore.clear()
                    reminderEnabledPlaceId = null
                    return@reminder
                }
                val start = criteria.startCoordinates
                val destination = criteria.endCoordinates
                val deadline = criteria.arrivalDeadlineEpochMillis
                val stopLatitude = recommendation.place.latitude
                val stopLongitude = recommendation.place.longitude
                val latestDeparture = recommendation.latestDepartureEpochMillis
                if (
                    start == null || destination == null || deadline == null ||
                    stopLatitude == null || stopLongitude == null || latestDeparture == null
                ) {
                    Toast.makeText(context, "알림에 필요한 경로 정보가 부족해요.", Toast.LENGTH_SHORT).show()
                    return@reminder
                }
                if (latestDeparture <= System.currentTimeMillis()) {
                    Toast.makeText(context, "출발 권장시각이 지나 알림을 설정할 수 없어요.", Toast.LENGTH_SHORT).show()
                    return@reminder
                }
                val stop = Coordinates(stopLatitude, stopLongitude)
                val navigationUrl = buildKakaoMapMultiRouteUrl(
                    startName = criteria.startName,
                    start = start,
                    waypoints = listOf(recommendation.place.name to stop),
                    destinationName = criteria.endName,
                    destination = destination,
                )
                val trip = ActiveTrip(
                    startName = criteria.startName,
                    start = start,
                    destinationName = criteria.endName,
                    destination = destination,
                    stopId = recommendation.place.id,
                    stopName = recommendation.place.name,
                    stop = stop,
                    arrivalDeadlineEpochMillis = deadline,
                    latestDepartureEpochMillis = latestDeparture,
                    navigationUrl = navigationUrl,
                    expiresAtEpochMillis = activeTripExpiryEpochMillis(deadline),
                )
                val hasNotificationPermission =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
                if (hasNotificationPermission) {
                    application.container.activeTripStore.save(trip)
                    application.container.departureReminderScheduler.schedule(trip)
                    reminderEnabledPlaceId = recommendation.place.id
                } else {
                    pendingReminderTrip = trip
                    pendingReminderPlaceId = recommendation.place.id
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onBack = {
                routeViewModel.startNewSearch()
                screen = AppDestination.LOCATION
            },
            onNewSearch = {
                routeViewModel.startNewSearch()
                screen = AppDestination.LOCATION
            },
            onNavigate = { recommendation ->
                openRoute(listOfNotNull(recommendation))
            },
            onDetail = { recommendation ->
                selected = recommendation
                screen = AppDestination.DETAIL
            },
        )

        AppDestination.DETAIL -> selected?.let {
            DetailScreen(
                criteria = criteria,
                recommendation = it,
                warning = routeState.warning,
                isSaved = savedPlaces.any { entry -> entry.place.id == it.place.id },
                onToggleSave = {
                    appScope.launch {
                        savedPlacesRepository.toggleGuest(it.place, System.currentTimeMillis())
                    }
                },
                isOpeningRoute = false,
                onOpenRoute = {
                    routeViewModel.selectPlace(it.place.id)
                    openRoute(listOf(it))
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
internal fun BottomNavigation(
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

@Composable
private fun LocationPermissionSettingsDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("위치 권한을 켜 주세요") },
        text = { Text("앱 설정에서 위치 권한을 허용하거나 위치를 직접 검색해 주세요.") },
        confirmButton = { TextButton(onClick = onOpenSettings) { Text("앱 설정 열기") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
private fun LoadingScreen(
    onBack: () -> Unit,
) {
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
                Text(
                    "가는 길 주변에서\n들를 장소를 찾고 있어요.",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                listOf("기본 경로 확인", "실시간 경유 경로 비교", "최대 체류시간 계산", "도착 전 여유 적용").forEach {
                    Text(
                        "✓  $it",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        color = TteumMuted,
                    )
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBack) {
                    Text("경로 입력으로 돌아가기")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
                            text = recommendation.maximumStayMinutes?.let { "최대 체류 약 ${it}분" }
                                ?: "체류시간 확인 필요",
                            modifier = Modifier.weight(0.9f),
                        )
                        DetailMetric(
                            icon = Icons.Default.DirectionsCar,
                            text = "${criteria.startName.ifBlank { "출발지" }}에서 ${recommendation.place.firstLegMinutes}분  |  추가 이동 +${recommendation.place.detourMinutes}분",
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
internal fun RouteMap(
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
                    detourMinutes = candidate.place.detourMinutes,
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
                                candidate.detourMinutes,
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
                                candidate.detourMinutes,
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
    detourMinutes: Int,
    selectedOrder: Int?,
    isFocused: Boolean,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val width = 68f * density
    val height = 40f * density
    return Bitmap.createBitmap(width.roundToInt(), height.roundToInt(), Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = android.graphics.Canvas(bitmap)
        val isSelected = selectedOrder != null
        val markerColor = if (isSelected) TteumRed else RouteFocusBlue
        if (isFocused) {
            val halo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = RouteFocusBlue.copy(alpha = 0.18f).toArgb()
            }
            canvas.drawRoundRect(0f, 0f, width, height, 20f * density, 20f * density, halo)
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
        val inset = if (isFocused) 2f * density else 5f * density
        canvas.drawRoundRect(inset, inset, width - inset, height - inset, 18f * density, 18f * density, background)
        canvas.drawRoundRect(inset, inset, width - inset, height - inset, 18f * density, 18f * density, border)
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = markerColor.toArgb()
            textSize = 14f * density
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val baseline = height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText("+${detourMinutes}분", width / 2f, baseline, textPaint)
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
