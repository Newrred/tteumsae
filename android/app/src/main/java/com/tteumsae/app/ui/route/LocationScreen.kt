package com.tteumsae.app.ui.route

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.LocationSearchResult
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.route.RouteFlowInput
import com.tteumsae.app.domain.route.RouteLocation
import com.tteumsae.app.domain.route.isValidArrivalDeadline
import com.tteumsae.app.domain.route.resolveArrivalDeadline
import com.tteumsae.app.platform.openAppSettings
import com.tteumsae.app.ui.deniedLocationPermissionNeedsSettings
import com.tteumsae.app.ui.hasLocationPermission
import com.tteumsae.app.ui.networkFailureMessage
import com.tteumsae.app.ui.requestCurrentLocation
import com.tteumsae.app.ui.shouldAutoLocateStart
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

internal fun canContinueRouteInput(
    input: RouteFlowInput,
    nowEpochMillis: Long,
    isBusy: Boolean,
): Boolean = !isBusy &&
    input.start != null &&
    input.destination != null &&
    input.arrivalDeadlineEpochMillis?.let {
        isValidArrivalDeadline(it, nowEpochMillis)
    } == true

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
internal fun RouteLocationScreen(
    input: RouteFlowInput,
    errorMessage: String?,
    searchPlaces: suspend (String, Boolean) -> List<LocationSearchResult>,
    resolveCurrentAddress: suspend (Coordinates) -> String,
    onStartSelected: (RouteLocation?) -> Unit,
    onDestinationSelected: (RouteLocation?) -> Unit,
    onDeadlineSelected: (Long?) -> Unit,
    onFiltersChanged: (Set<PlaceCategory>) -> Unit,
    isChecking: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible
    var startQuery by rememberSaveable {
        mutableStateOf(input.start?.name ?: "현재 위치")
    }
    var destinationQuery by rememberSaveable {
        mutableStateOf(input.destination?.name.orEmpty())
    }
    var isLocating by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val nowEpochMillis = System.currentTimeMillis()

    val locateCurrentPosition: () -> Unit = {
        isLocating = true
        requestCurrentLocation(
            context = context,
            onSuccess = { location ->
                val routeLocation = RouteLocation(
                    name = "현재 위치",
                    coordinates = Coordinates(location.latitude, location.longitude),
                )
                startQuery = routeLocation.name
                onStartSelected(routeLocation)
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
        } else if (
            deniedLocationPermissionNeedsSettings(permissions) { permission ->
                (context as? android.app.Activity)
                    ?.shouldShowRequestPermissionRationale(permission) == true
            }
        ) {
            showPermissionSettingsDialog = true
        } else {
            Toast.makeText(
                context,
                "현재 위치 대신 출발지를 직접 검색할 수 있어요.",
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

    LaunchedEffect(startQuery, input.start) {
        if (shouldAutoLocateStart(startQuery, input.start != null)) useCurrentLocation()
    }
    LaunchedEffect(input.start?.coordinates, input.start?.name) {
        val start = input.start ?: return@LaunchedEffect
        if (start.name != "현재 위치") return@LaunchedEffect
        val address = try {
            resolveCurrentAddress(start.coordinates)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            null
        }
        if (!address.isNullOrBlank()) {
            startQuery = address
            onStartSelected(start.copy(name = address))
        }
    }

    val busy = isChecking || isLocating
    val canContinue = canContinueRouteInput(input, nowEpochMillis, busy)
    BackHandler(enabled = isImeVisible) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.White,
        bottomBar = {
            if (!isImeVisible) {
                Surface(color = androidx.compose.ui.graphics.Color.White) {
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
                            if (isChecking) "경로 확인 중..." else "틈새 찾기",
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
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
        ) {
            item {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "어디로, 몇 시까지 가나요?",
                    fontSize = 31.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "도착 시간에 늦지 않는 선에서 들를 곳을 찾아드려요.",
                    color = TteumMuted,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(28.dp))
                Surface(
                    color = androidx.compose.ui.graphics.Color(0xFFF5F6F8),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column {
                        RouteLocationSearchField(
                            value = startQuery,
                            selected = input.start,
                            label = "출발지",
                            labelBackground = TteumMuted,
                            searchPlaces = searchPlaces,
                            gangwonOnly = false,
                            onValueChange = {
                                startQuery = it
                                onStartSelected(null)
                            },
                            onSelected = {
                                startQuery = it.name
                                onStartSelected(RouteLocation(it.name, it.coordinates))
                            },
                            onUseCurrentLocation = useCurrentLocation,
                            isLocating = isLocating,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = androidx.compose.ui.graphics.Color(0xFFDDE0E5),
                        )
                        RouteLocationSearchField(
                            value = destinationQuery,
                            selected = input.destination,
                            label = "목적지",
                            labelBackground = TteumInk,
                            searchPlaces = searchPlaces,
                            gangwonOnly = true,
                            onValueChange = {
                                destinationQuery = it
                                onDestinationSelected(null)
                            },
                            onSelected = {
                                destinationQuery = it.name
                                onDestinationSelected(RouteLocation(it.name, it.coordinates))
                            },
                            autoFocus = input.destination == null && input.start != null,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("도착 마감", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    color = TteumRedSoft,
                    contentColor = TteumRed,
                    border = BorderStroke(1.dp, TteumRed.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                input.arrivalDeadlineEpochMillis?.let(::formatDeadline)
                                    ?: "도착해야 하는 시각 선택",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "선택한 시각은 확인을 눌러야 적용돼요.",
                                color = TteumMuted,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("원하는 분위기 · 선택", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text("고르지 않아도 바로 추천받을 수 있어요.", color = TteumMuted, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RouteFilterChip(
                        label = "아무거나",
                        selected = input.categories.isEmpty(),
                        onClick = { onFiltersChanged(emptySet()) },
                    )
                    PlaceCategory.entries.forEach { category ->
                        RouteFilterChip(
                            label = category.label,
                            selected = category in input.categories,
                            onClick = {
                                val updated = input.categories.toMutableSet().apply {
                                    if (!add(category)) remove(category)
                                }
                                onFiltersChanged(updated)
                            },
                        )
                    }
                }
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Text(errorMessage, color = TteumRed, fontSize = 13.sp)
                }
            }
        }
    }

    if (showTimePicker) {
        ArrivalDeadlinePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                onDeadlineSelected(
                    resolveArrivalDeadline(
                        selectedHour = hour,
                        selectedMinute = minute,
                        nowEpochMillis = System.currentTimeMillis(),
                    ),
                )
                showTimePicker = false
            },
        )
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
                ) { Text("위치 설정 열기") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) { Text("취소") }
            },
        )
    }
    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("위치 권한을 켜 주세요") },
            text = { Text("앱 설정에서 위치 권한을 허용하거나 출발지를 직접 검색해 주세요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        openAppSettings(context)
                    },
                ) { Text("앱 설정 열기") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) { Text("직접 검색") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArrivalDeadlinePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val initial = Instant.ofEpochMilli(System.currentTimeMillis())
        .atZone(ZoneId.of("Asia/Seoul"))
        .plusHours(1)
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("도착 마감 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                TimePicker(state = pickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(role = Role.Checkbox, onClick = onClick),
        color = if (selected) TteumRedSoft else androidx.compose.ui.graphics.Color(0xFFF5F6F8),
        contentColor = if (selected) TteumRed else TteumMuted,
        border = if (selected) BorderStroke(1.dp, TteumRed) else null,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RouteLocationSearchField(
    value: String,
    selected: RouteLocation?,
    label: String,
    searchPlaces: suspend (String, Boolean) -> List<LocationSearchResult>,
    gangwonOnly: Boolean,
    onValueChange: (String) -> Unit,
    onSelected: (LocationSearchResult) -> Unit,
    labelBackground: androidx.compose.ui.graphics.Color,
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

    LaunchedEffect(autoFocus, selected) {
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
    LaunchedEffect(value, selected, searchAttempt) {
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
            if (error is CancellationException) throw error
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
            Surface(color = labelBackground, shape = RoundedCornerShape(8.dp)) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
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
                } else {
                    if (value.isBlank()) Text("장소 검색", color = TteumMuted)
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
            if (onUseCurrentLocation != null) {
                Spacer(Modifier.width(10.dp))
                Surface(
                    modifier = Modifier.clickable(enabled = !isLocating, onClick = onUseCurrentLocation),
                    color = TteumRedSoft,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(
                        modifier = Modifier.widthIn(min = 48.dp).heightIn(min = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (isLocating) "확인 중" else "현위치",
                            color = TteumRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        if (results.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color.White)) {
                results.take(5).forEachIndexed { index, result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (index == 0) Modifier.bringIntoViewRequester(firstResultRequester) else Modifier,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(searchMessage.orEmpty(), modifier = Modifier.weight(1f), color = TteumMuted, fontSize = 12.sp)
                if (searchFailed) TextButton(onClick = { searchAttempt += 1 }) { Text("다시 시도") }
            }
        }
    }
}

private fun formatDeadline(epochMillis: Long): String {
    val zoneId = ZoneId.of("Asia/Seoul")
    val selected = Instant.ofEpochMilli(epochMillis).atZone(zoneId)
    val today = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate()
    val dayPrefix = when (selected.toLocalDate()) {
        today -> "오늘"
        today.plusDays(1) -> "내일"
        else -> selected.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
    }
    return "$dayPrefix ${selected.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))}까지"
}
