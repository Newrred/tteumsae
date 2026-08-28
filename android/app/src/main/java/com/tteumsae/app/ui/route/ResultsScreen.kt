package com.tteumsae.app.ui.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed

@Composable
internal fun RouteResultsScreen(
    criteria: SearchCriteria,
    recommendations: List<SafeRecommendation>,
    baseRoute: RouteSummary?,
    corridorRadiusMeters: Int,
    selectedPlaceId: String?,
    warning: String,
    isRefreshing: Boolean,
    onSelectPlace: (String) -> Unit,
    onClearSelection: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onNewSearch: () -> Unit,
    onNavigate: (SafeRecommendation?) -> Unit,
    onDetail: (SafeRecommendation) -> Unit,
) {
    val selected = recommendations.firstOrNull { it.place.id == selectedPlaceId }
    val focusedPlaceId = selectedPlaceId ?: recommendations.firstOrNull()?.place?.id
    val directRouteIsTight = baseRoute != null &&
        baseRoute.totalDrivingMinutes + criteria.safetyBufferMinutes > criteria.deadlineMinutesFromNow

    Scaffold(
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Button(
                    onClick = { onNavigate(selected) },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        if (selected == null) "목적지로 바로 안내" else "이곳 들러 카카오맵 안내",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            RouteMapCanvas(
                modifier = Modifier.fillMaxSize(),
                criteria = criteria,
                selected = selected,
                baseRoute = baseRoute,
                recommendations = recommendations,
                selectedPlaceId = selectedPlaceId,
                focusedPlaceId = focusedPlaceId,
                corridorRadiusMeters = corridorRadiusMeters,
                onCandidateClick = { tapped ->
                    when (nextSelectedPlaceId(selectedPlaceId, tapped)) {
                        null -> onClearSelection()
                        else -> onSelectPlace(tapped)
                    }
                },
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                color = Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                    Column(Modifier.weight(1f)) {
                        Text("한 곳만 골라 가볍게 들러보세요", fontWeight = FontWeight.Bold)
                        Text("핀에는 추가 이동시간만 표시해요", color = TteumMuted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "현재 교통으로 다시 확인")
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .heightIn(max = 390.dp),
                color = Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp,
            ) {
                if (recommendations.isEmpty()) {
                    Column(Modifier.padding(24.dp)) {
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
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (warning.isNotBlank()) {
                            item { Text(warning, color = TteumMuted, fontSize = 12.sp) }
                        }
                        item {
                            TextButton(onClick = onRefresh, enabled = !isRefreshing) {
                                Text("현재 교통으로 다시 확인")
                            }
                        }
                        items(recommendations, key = { it.place.id }) { recommendation ->
                            RouteCandidateCard(
                                recommendation = recommendation,
                                selected = recommendation.place.id == selectedPlaceId,
                                onSelect = {
                                    when (nextSelectedPlaceId(selectedPlaceId, recommendation.place.id)) {
                                        null -> onClearSelection()
                                        else -> onSelectPlace(recommendation.place.id)
                                    }
                                },
                                onDetail = { onDetail(recommendation) },
                            )
                        }
                    }
                }
            }
        }
    }
}
