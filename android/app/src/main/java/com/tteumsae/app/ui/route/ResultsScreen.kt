package com.tteumsae.app.ui.route

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.domain.route.RouteNavigationAction
import com.tteumsae.app.domain.route.recommendationNeedsRecheck
import com.tteumsae.app.domain.route.routeNavigationAction
import com.tteumsae.app.location.LocationAccessPolicy
import com.tteumsae.app.ui.theme.TteumMuted
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
    var clusterScopeIds by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val visibleRecommendations = clusterScopeIds?.let { ids ->
        recommendations.filter { it.place.id in ids }
    }?.takeIf { it.isNotEmpty() } ?: recommendations
    var overviewRequestId by remember { mutableIntStateOf(0) }
    var sheetPosition by rememberSaveable { mutableStateOf(ResultSheetPosition.BALANCED) }
    var revealRequestId by remember { mutableIntStateOf(0) }
    var revealPlaceId by remember { mutableStateOf<String?>(null) }
    var nowEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val recommendationListState = rememberLazyListState()
    val deadlineCannotBeRechecked = arrivalDeadlineCannotBeRechecked(criteria, nowEpochMillis)
    val navigationAction = routeNavigationAction(
        criteria, listOfNotNull(selected), calculatedAtEpochMillis, nowEpochMillis,
    )
    val directRouteIsTight = baseRoute != null &&
        baseRoute.totalDrivingMinutes + criteria.safetyBufferMinutes > criteria.deadlineMinutesFromNow
    val fontScale = LocalDensity.current.fontScale

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowEpochMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(recommendations.map { it.place.id }) {
        clusterScopeIds = clusterScopeIds?.intersect(recommendations.map { it.place.id }.toSet())
            ?.takeIf { it.isNotEmpty() }?.toList()
    }

    // Explicit pin/cluster actions reveal a row once. Merely browsing, refreshing or
    // expanding the sheet must not move the list away from the user's reading position.
    LaunchedEffect(revealRequestId) {
        if (revealRequestId == 0) return@LaunchedEffect
        val id = revealPlaceId ?: return@LaunchedEffect
        withFrameNanos { }
        resultCandidateRevealIndex(visibleRecommendations.map { it.place.id }, id, warning.isNotBlank())
            ?.let { recommendationListState.animateScrollToItem(it) }
    }
    BackHandler(enabled = sheetPosition == ResultSheetPosition.LIST) {
        sheetPosition = ResultSheetPosition.BALANCED
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Surface(
                    modifier = Modifier
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
                                } else if (fontScale > 1.3f) {
                                    "경유지 선택"
                                } else {
                                    "들를 곳을 골라보세요"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                            )
                            Text(
                                if (recommendations.isEmpty()) {
                                    "조건을 바꾸거나 목적지로 바로 이동할 수 있어요"
                                } else {
                                    resultFreshnessLabel(calculatedAtEpochMillis, nowEpochMillis)
                                },
                                color = TteumMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            )
                            if (!LocationAccessPolicy.automaticLocationEnabled) {
                                Text(
                                    "선택한 출발지 기준",
                                    color = TteumMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                )
                            }
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
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column {
                Button(
                    onClick = {
                        when (routeNavigationAction(
                            criteria, listOfNotNull(selected), calculatedAtEpochMillis, System.currentTimeMillis(),
                        )) {
                            RouteNavigationAction.RESET_DEADLINE -> onNewSearch()
                            RouteNavigationAction.RECHECK -> onRefresh()
                            RouteNavigationAction.OPEN_ROUTE -> onNavigate(selected)
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
                            navigationAction == RouteNavigationAction.RESET_DEADLINE -> "도착 마감 다시 정하기"
                            navigationAction == RouteNavigationAction.RECHECK -> "현재 교통으로 다시 확인"
                            selected == null -> "목적지로 바로 안내"
                            else -> "이곳 들러 카카오맵 안내"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
                }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ResultMapSheetLayout(
                listState = recommendationListState,
                position = sheetPosition,
                onPositionSettled = { sheetPosition = it },
                header = { dragModifier ->
                    ResultSheetHeader(
                        modifier = dragModifier,
                        count = visibleRecommendations.size,
                        calculatedAtEpochMillis = calculatedAtEpochMillis,
                        isClusterScope = clusterScopeIds != null,
                        position = sheetPosition,
                        onShowAll = { clusterScopeIds = null },
                        onPositionChanged = { sheetPosition = it },
                    )
                },
                mapContent = { obscuredHeight, visibleHeight, availableHeight ->
                val mapInsets = resultMapViewportInsets(availableHeight, obscuredHeight)
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
                    mapTopPadding = mapInsets.topDp.dp,
                    mapBottomPadding = mapInsets.bottomDp.dp,
                    onMapInteraction = {
                        if (clusterScopeIds != null) clusterScopeIds = null
                    },
                    onCandidateClick = { tapped ->
                        if (clusterScopeIds?.contains(tapped) == false) clusterScopeIds = null
                        when (nextSelectedPlaceId(selectedPlaceId, tapped)) {
                            null -> onClearSelection()
                            else -> {
                                onSelectPlace(tapped)
                                revealPlaceId = tapped
                                revealRequestId += 1
                                sheetPosition = ResultSheetPosition.BALANCED
                            }
                        }
                    },
                    onClusterClick = { memberIds ->
                        clusterScopeIds = memberIds.toList()
                        // Browsing a cluster is not a change to the chosen trip or reminder.
                        revealPlaceId = memberIds.firstOrNull()
                        revealRequestId += 1
                        sheetPosition = ResultSheetPosition.BALANCED
                    },
                )
                if (visibleHeight > 72f) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        shadowElevation = 3.dp,
                    ) {
                        TextButton(onClick = { overviewRequestId += 1 }) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("전체 경로", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                },
                sheetContent = {
                    LazyColumn(
                        state = recommendationListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 20.dp),
                    ) {
                        if (recommendations.isEmpty()) {
                            item(key = "empty-results") {
                                EmptyResultSheet(directRouteIsTight, warning, onNewSearch)
                            }
                        }
                        if (warning.isNotBlank() && recommendations.isNotEmpty()) {
                            item(key = "result-warning") {
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
                                needsRecheck = recommendationNeedsRecheck(
                                    recommendation,
                                    calculatedAtEpochMillis,
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
                                                !recommendationNeedsRecheck(
                                                    recommendation,
                                                    calculatedAtEpochMillis,
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
                },
            )
        }
    }
}

@Composable
private fun ResultSheetHeader(
    modifier: Modifier,
    count: Int,
    calculatedAtEpochMillis: Long?,
    isClusterScope: Boolean,
    position: ResultSheetPosition,
    onShowAll: () -> Unit,
    onPositionChanged: (ResultSheetPosition) -> Unit,
) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    Column(modifier) {
    Box(Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(36.dp).height(4.dp).background(Color(0xFFD3D6DB), RoundedCornerShape(50)))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    isClusterScope && largeText -> "주변 ${count}곳"
                    isClusterScope -> "이 주변 ${count}곳"
                    largeText -> "추천 ${count}곳"
                    else -> "경로 주변 추천 ${count}곳"
                },
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${calculatedTimeLabel(calculatedAtEpochMillis)} 계산 기준 · 한 곳 선택",
                color = TteumMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = { onPositionChanged(ResultSheetPosition.MAP) },
            enabled = position != ResultSheetPosition.MAP,
            modifier = Modifier.semantics { contentDescription = "지도 크게 보기" },
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
        }
        IconButton(
            onClick = {
                onPositionChanged(if (position == ResultSheetPosition.LIST) ResultSheetPosition.BALANCED else ResultSheetPosition.LIST)
            },
            modifier = Modifier.semantics {
                contentDescription = if (position == ResultSheetPosition.LIST) "지도와 목록 함께 보기" else "목록 펼치기"
            },
        ) {
            Icon(
                if (position == ResultSheetPosition.LIST) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
            )
        }
    }
    if (isClusterScope) {
        TextButton(onClick = onShowAll, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("전체 추천 보기", color = TteumRed, fontWeight = FontWeight.SemiBold)
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
            .toggleable(value = checked, enabled = enabled, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = onCheckedChange)
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
                "${reminderTimeLabel(recommendation)} 출발 알림 예정",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                if (!enabled) {
                    "현재 교통으로 다시 확인한 뒤 켜 주세요"
                } else if (checked) {
                    "출발 5분 전 예정 · 기기 절전 등으로 늦어질 수 있어요"
                } else {
                    "선택 알림 · 기기 절전 등으로 늦어질 수 있어요"
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
