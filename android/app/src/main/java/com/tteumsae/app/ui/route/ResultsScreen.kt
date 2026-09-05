package com.tteumsae.app.ui.route

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumHandle
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteResultsScreen(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
    baseRoute: RouteSummary?,
    corridorRadiusMeters: Int,
    selectedPlaceId: String?,
    warning: String,
    calculatedAtEpochMillis: Long?,
    isRefreshing: Boolean,
    reminderEnabled: Boolean,
    onSelectPlace: (String) -> Unit,
    onClearSelection: () -> Unit,
    onRefresh: () -> Unit,
    onReminderChanged: (SafeRecommendation, Boolean) -> Unit,
    onBack: () -> Unit,
    onNewSearch: () -> Unit,
    onNavigate: (SafeRecommendation?) -> Unit,
    onDetail: (SafeRecommendation) -> Unit,
) {
    val selected = recommendations.firstOrNull { it.place.id == selectedPlaceId }
    val focusedPlaceId = selectedPlaceId
    var clusterScopeIds by remember { mutableStateOf<Set<String>?>(null) }
    val visibleRecommendations = clusterScopeIds?.let { ids ->
        recommendations.filter { it.place.id in ids }
    }?.takeIf { it.isNotEmpty() } ?: recommendations
    var overviewRequestId by remember { mutableIntStateOf(0) }
    var nowEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val recommendationListState = rememberLazyListState()
    val deadlineHasPassed = arrivalDeadlineHasPassed(criteria, nowEpochMillis)
    val deadlineCannotBeRechecked = arrivalDeadlineCannotBeRechecked(criteria, nowEpochMillis)
    val selectedDepartureHasPassed = selected?.let {
        recommendationDepartureHasPassed(it, nowEpochMillis)
    } == true
    val selectedNeedsNewDeadline = selectedDepartureHasPassed &&
        arrivalDeadlineCannotBeRechecked(criteria, nowEpochMillis)
    val directRouteIsTight = baseRoute != null &&
        baseRoute.totalDrivingMinutes + criteria.safetyBufferMinutes > criteria.deadlineMinutesFromNow
    val fontScale = LocalDensity.current.fontScale
    val sheetPeekHeight by animateDpAsState(
        targetValue = when {
            recommendations.isEmpty() -> 210.dp
            selected != null -> selectedResultPeekHeightDp(fontScale).dp
            else -> 380.dp
        },
        animationSpec = tween(280),
        label = "result-sheet-peek-height",
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowEpochMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(recommendations.map { it.place.id }) {
        clusterScopeIds = clusterScopeIds?.intersect(recommendations.map { it.place.id }.toSet())
            ?.takeIf { it.isNotEmpty() }
    }

    LaunchedEffect(selectedPlaceId, visibleRecommendations, warning) {
        val selectedIndex = visibleRecommendations.indexOfFirst { it.place.id == selectedPlaceId }
        if (selectedIndex >= 0) {
            val targetIndex = selectedIndex + 1 + if (warning.isNotBlank()) 1 else 0
            recommendationListState.animateScrollToItem(targetIndex)
            // The selected row and sheet both grow. Re-anchor after those
            // animations so the chosen place, rather than the previous row,
            // remains at the visual top of the list.
            delay(360)
            recommendationListState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        when {
                            deadlineHasPassed -> onNewSearch()
                            selectedNeedsNewDeadline -> onNewSearch()
                            selectedDepartureHasPassed -> onRefresh()
                            else -> onNavigate(selected)
                        }
                    },
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .heightIn(min = 56.dp)
                        .semantics {
                            stateDescription = selected?.let {
                                "${it.place.name} 경유 선택됨"
                            } ?: "경유지 선택 안 함"
                        },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        when {
                            deadlineHasPassed -> "도착 마감 다시 정하기"
                            selectedNeedsNewDeadline -> "도착 마감 다시 정하기"
                            selectedDepartureHasPassed -> "출발 시각 지남 · 다시 확인"
                            selected == null -> "목적지로 바로 안내"
                            else -> "이곳 들러 카카오맵 안내"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        BottomSheetScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            sheetPeekHeight = sheetPeekHeight,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContainerColor = Color.White,
            sheetShadowElevation = 12.dp,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(color = TteumHandle)
            },
            sheetContent = {
                if (recommendations.isEmpty()) {
                    EmptyResultSheet(
                        directRouteIsTight = directRouteIsTight,
                        warning = warning,
                        onNewSearch = onNewSearch,
                    )
                } else {
                    LazyColumn(
                        state = recommendationListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp, max = 590.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                    ) {
                        item(key = "result-sheet-header") {
                            ResultSheetHeader(
                                count = visibleRecommendations.size,
                                calculatedAtEpochMillis = calculatedAtEpochMillis,
                                isClusterScope = clusterScopeIds != null,
                                onShowAll = { clusterScopeIds = null },
                            )
                        }
                        if (warning.isNotBlank()) {
                            item {
                                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    ResultWarningBanner(
                                        message = warning,
                                        isRefreshing = isRefreshing,
                                        actionLabel = if (deadlineCannotBeRechecked) {
                                            "시간 다시 정하기"
                                        } else {
                                            "다시 확인"
                                        },
                                        onAction = if (deadlineCannotBeRechecked) onNewSearch else onRefresh,
                                    )
                                }
                            }
                        }
                        itemsIndexed(
                            items = visibleRecommendations,
                            key = { _, item -> item.place.id },
                        ) { index, recommendation ->
                            val isSelected = recommendation.place.id == selectedPlaceId
                            RouteCandidateCard(
                                recommendation = recommendation,
                                selected = isSelected,
                                departureHasPassed = recommendationDepartureHasPassed(
                                    recommendation,
                                    nowEpochMillis,
                                ),
                                onSelect = {
                                    when (nextSelectedPlaceId(selectedPlaceId, recommendation.place.id)) {
                                        null -> onClearSelection()
                                        else -> onSelectPlace(recommendation.place.id)
                                    }
                                },
                                onDetail = { onDetail(recommendation) },
                                reminderContent = if (isSelected) {
                                    {
                                        ReminderToggle(
                                            recommendation = recommendation,
                                            checked = reminderEnabled,
                                            enabled = reminderEnabled ||
                                                !recommendationDepartureHasPassed(
                                                    recommendation,
                                                    nowEpochMillis,
                                                ),
                                            onCheckedChange = {
                                                onReminderChanged(recommendation, it)
                                            },
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                            if (index != visibleRecommendations.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFFE9EBEF),
                                )
                            }
                        }
                    }
                }
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                RouteMapCanvas(
                    modifier = Modifier.fillMaxSize(),
                    criteria = criteria,
                    selected = selected,
                    baseRoute = baseRoute,
                    recommendations = recommendations,
                    selectedPlaceId = selectedPlaceId,
                    focusedPlaceId = focusedPlaceId,
                    corridorRadiusMeters = corridorRadiusMeters,
                    overviewRequestId = overviewRequestId,
                    mapBottomPadding = sheetPeekHeight + 20.dp,
                    onMapInteraction = {
                        if (clusterScopeIds != null) clusterScopeIds = null
                    },
                    onCandidateClick = { tapped ->
                        when (nextSelectedPlaceId(selectedPlaceId, tapped)) {
                            null -> onClearSelection()
                            else -> onSelectPlace(tapped)
                        }
                    },
                    onClusterClick = { memberIds ->
                        clusterScopeIds = memberIds.toSet()
                        if (selectedPlaceId != null && selectedPlaceId !in memberIds) {
                            onClearSelection()
                        }
                    },
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.97f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (recommendations.isEmpty()) {
                                    "추천 가능한 장소가 없어요"
                                } else {
                                    "들를 곳을 골라보세요"
                                },
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (recommendations.isEmpty()) {
                                    "조건을 바꾸거나 목적지로 바로 이동할 수 있어요"
                                } else {
                                    resultMapCaption(
                                        calculatedAtEpochMillis,
                                        recommendations.size,
                                    )
                                },
                                color = TteumMuted,
                                fontSize = 12.sp,
                            )
                        }
                        IconButton(
                            onClick = if (deadlineCannotBeRechecked) onNewSearch else onRefresh,
                            enabled = !isRefreshing,
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = if (deadlineCannotBeRechecked) {
                                        "도착 마감 다시 정하기"
                                    } else {
                                        "현재 교통으로 다시 확인"
                                    },
                                )
                            }
                        }
                    }
                }

                if (selected != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 82.dp, end = 16.dp),
                        color = Color.White.copy(alpha = 0.96f),
                        shape = RoundedCornerShape(50),
                        shadowElevation = 5.dp,
                    ) {
                        TextButton(onClick = { overviewRequestId += 1 }) {
                            Text("전체 경로", color = TteumRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSheetHeader(
    count: Int,
    calculatedAtEpochMillis: Long?,
    isClusterScope: Boolean,
    onShowAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (isClusterScope) "이 주변 ${count}곳" else "경로 주변 추천 ${count}곳",
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${calculatedTimeLabel(calculatedAtEpochMillis)} 현재 교통 반영 · 한 곳 선택",
                color = TteumMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
        if (isClusterScope) {
            TextButton(onClick = onShowAll) {
                Text("전체 보기", color = TteumRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ResultWarningBanner(
    message: String,
    isRefreshing: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TteumRedSoft,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = TteumRed,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            TextButton(onClick = onAction, enabled = !isRefreshing) {
                Text(if (isRefreshing) "확인 중" else actionLabel)
            }
        }
    }
}

@Composable
private fun ReminderToggle(
    recommendation: SafeRecommendation,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = if (enabled) TteumRed else TteumMuted,
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${reminderTimeLabel(recommendation)}에 출발 알림",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                if (!enabled) {
                    "권장 출발시각이 지나 설정할 수 없어요"
                } else if (checked) {
                    "권장 출발 5분 전에 알려드려요"
                } else {
                    "필요하면 켜 주세요"
                },
                color = TteumMuted,
                fontSize = 11.sp,
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun EmptyResultSheet(
    directRouteIsTight: Boolean,
    warning: String,
    onNewSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Text(
            if (directRouteIsTight) {
                "지금 바로 출발해도 도착 마감이 빠듯해요."
            } else {
                "지금 조건에서 15분 이상 머물 수 있는 장소가 없어요."
            },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            warning.ifBlank { "목적지로 바로 안내받거나 도착 마감을 다시 정해보세요." },
            color = TteumMuted,
        )
        TextButton(onClick = onNewSearch) { Text("경로 다시 정하기") }
    }
}

private fun calculatedTimeLabel(epochMillis: Long?): String {
    if (epochMillis == null) return "지금"
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.of("Asia/Seoul"))
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

private fun resultMapCaption(epochMillis: Long?, count: Int): String =
    "${calculatedTimeLabel(epochMillis)} 현재 교통 · ${count}곳"
