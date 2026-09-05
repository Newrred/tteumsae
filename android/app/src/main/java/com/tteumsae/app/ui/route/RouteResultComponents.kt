package com.tteumsae.app.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.OperationStatus
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun detourPinLabel(recommendation: SafeRecommendation): String =
    "+${recommendation.place.detourMinutes}분"

internal fun nextSelectedPlaceId(current: String?, tapped: String): String? =
    tapped.takeUnless { it == current }

internal fun selectedResultPeekHeightDp(fontScale: Float): Float =
    (420f * fontScale.coerceAtLeast(1f))
        .coerceAtMost(590f)

internal fun maximumStayLabel(recommendation: SafeRecommendation): String =
    recommendation.maximumStayMinutes?.let {
        "여기서 최대 약 ${readableDuration(it)} 머물 수 있어요"
    }
        ?: "체류 가능 시간을 다시 확인해 주세요"

internal fun latestDepartureLabel(
    recommendation: SafeRecommendation,
    zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
): String = recommendation.latestDepartureEpochMillis?.let { epochMillis ->
    val formatted = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("a h시 m분", Locale.KOREAN))
    "늦어도 ${formatted} 출발을 권장해요"
} ?: "출발 권장시각을 다시 확인해 주세요"

internal fun recommendationDepartureHasPassed(
    recommendation: SafeRecommendation,
    nowEpochMillis: Long,
): Boolean = recommendation.latestDepartureEpochMillis?.let { latestDeparture ->
    nowEpochMillis >= latestDeparture
} == true

internal fun arrivalDeadlineHasPassed(
    criteria: com.tteumsae.app.domain.SearchCriteria,
    nowEpochMillis: Long,
): Boolean = criteria.arrivalDeadlineEpochMillis?.let { deadline ->
    nowEpochMillis >= deadline
} == true

internal fun arrivalDeadlineCannotBeRechecked(
    criteria: com.tteumsae.app.domain.SearchCriteria,
    nowEpochMillis: Long,
): Boolean = criteria.arrivalDeadlineEpochMillis?.let { deadline ->
    deadline - nowEpochMillis < 15L * 60L * 1_000L
} == true

internal fun productNavigationRecommendations(
    selectedPlaceId: String?,
    recommendations: List<SafeRecommendation>,
): List<SafeRecommendation> = selectedPlaceId?.let { selected ->
    recommendations.firstOrNull { it.place.id == selected }?.let(::listOf)
}.orEmpty()

internal fun compactMaximumStayLabel(recommendation: SafeRecommendation): String =
    recommendation.maximumStayMinutes?.let(::readableDuration) ?: "확인 필요"

internal fun latestDepartureTimeLabel(
    recommendation: SafeRecommendation,
    zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
): String = recommendation.latestDepartureEpochMillis?.let { epochMillis ->
    Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))
} ?: "확인 필요"

internal fun reminderTimeLabel(
    recommendation: SafeRecommendation,
    zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
): String = recommendation.latestDepartureEpochMillis?.let { epochMillis ->
    Instant.ofEpochMilli(epochMillis - 5L * 60L * 1_000L)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))
} ?: "시각 확인 필요"

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RouteCandidateCard(
    recommendation: SafeRecommendation,
    selected: Boolean,
    departureHasPassed: Boolean = false,
    onSelect: () -> Unit,
    onDetail: () -> Unit,
    reminderContent: (@Composable () -> Unit)? = null,
) {
    val fontScale = LocalDensity.current.fontScale
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onSelect() },
            ),
        color = if (selected) Color(0xFFFFF7F8) else Color.White,
    ) {
        Box {
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .heightIn(min = 104.dp)
                        .background(TteumRed, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)),
                )
            }
            Column(Modifier.padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    PlaceCategoryIcon(
                        category = recommendation.place.category,
                        selected = selected,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            recommendation.place.name,
                            color = TteumInk,
                            fontSize = 17.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "경유 ${detourPinLabel(recommendation)} · 최대 ${compactMaximumStayLabel(recommendation)}",
                            color = Color(0xFF50545C),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ResultTag(
                                text = recommendation.place.category.label,
                                foreground = Color(0xFF50545C),
                                background = Color(0xFFF1F2F4),
                            )
                            if (recommendation.operationStatus == OperationStatus.OPEN) {
                                ResultTag(
                                    text = "운영 가능",
                                    foreground = Color(0xFF20724E),
                                    background = Color(0xFFEAF6EF),
                                )
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (selected) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                color = TteumRed,
                                shape = RoundedCornerShape(50),
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = Color.White,
                                    modifier = Modifier.padding(5.dp),
                                )
                            }
                        } else {
                            Text(
                                "선택",
                                color = TteumRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 3.dp, start = 8.dp),
                            )
                        }
                        TextButton(
                            onClick = onDetail,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .semantics {
                                    contentDescription = "${recommendation.place.name} 장소 정보 보기"
                                },
                        ) {
                            Text("상세보기 ›", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                AnimatedVisibility(visible = selected) {
                    Column {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = TteumRedSoft,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    "이 경유지를 선택하면",
                                    color = TteumRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.height(9.dp))
                                BoxWithConstraints {
                                    if (fontScale > 1.35f || maxWidth < 300.dp) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            SelectionMetric(
                                                label = "머물 수 있는 시간",
                                                value = compactMaximumStayLabel(recommendation),
                                            )
                                            SelectionMetric(
                                                label = "출발 권장",
                                                value = if (departureHasPassed) "다시 확인 필요" else latestDepartureTimeLabel(recommendation),
                                                emphasized = true,
                                            )
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                            SelectionMetric(
                                                label = "머물 수 있는 시간",
                                                value = compactMaximumStayLabel(recommendation),
                                                modifier = Modifier.weight(1f),
                                            )
                                            SelectionMetric(
                                                label = "출발 권장",
                                                value = if (departureHasPassed) "다시 확인 필요" else latestDepartureTimeLabel(recommendation),
                                                emphasized = true,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "장소까지 ${readableDuration(recommendation.place.firstLegMinutes)} · 경유 ${detourPinLabel(recommendation)}",
                                    color = Color(0xFF59606A),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                        reminderContent?.let {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFE9EBEF))
                            Spacer(Modifier.height(4.dp))
                            it()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaceCategoryIcon(
    category: PlaceCategory,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (category) {
        PlaceCategory.ATTRACTION -> Icons.Default.Landscape
        PlaceCategory.RESTAURANT -> Icons.Default.Restaurant
        PlaceCategory.CAFE -> Icons.Default.LocalCafe
        PlaceCategory.CULTURE -> Icons.Default.Museum
        PlaceCategory.FESTIVAL -> Icons.Default.Celebration
        PlaceCategory.SHOPPING -> Icons.Default.ShoppingBag
        PlaceCategory.LEISURE -> Icons.AutoMirrored.Filled.DirectionsRun
    }
    Surface(
        modifier = modifier.size(40.dp),
        color = if (selected) TteumRedSoft else categorySoftColor(category),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = if (selected) TteumRed else categoryStrongColor(category),
            modifier = Modifier.padding(9.dp),
        )
    }
}

@Composable
private fun ResultTag(
    text: String,
    foreground: Color,
    background: Color,
) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            color = foreground,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun SelectionMetric(
    label: String,
    value: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, color = Color(0xFF6C727C), fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = if (emphasized) TteumRed else TteumInk,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun categoryStrongColor(category: PlaceCategory): Color = when (category) {
    PlaceCategory.ATTRACTION -> Color(0xFF2F7D5B)
    PlaceCategory.RESTAURANT -> Color(0xFFC95622)
    PlaceCategory.CAFE -> Color(0xFF7A4A34)
    PlaceCategory.CULTURE -> Color(0xFF6248A8)
    PlaceCategory.FESTIVAL -> Color(0xFFB73869)
    PlaceCategory.SHOPPING -> Color(0xFF0F7278)
    PlaceCategory.LEISURE -> Color(0xFF2F6FE4)
}

private fun categorySoftColor(category: PlaceCategory): Color = when (category) {
    PlaceCategory.ATTRACTION -> Color(0xFFEAF5EF)
    PlaceCategory.RESTAURANT -> Color(0xFFFFEFE8)
    PlaceCategory.CAFE -> Color(0xFFF5ECE7)
    PlaceCategory.CULTURE -> Color(0xFFF0ECFA)
    PlaceCategory.FESTIVAL -> Color(0xFFFFEBF2)
    PlaceCategory.SHOPPING -> Color(0xFFE7F4F4)
    PlaceCategory.LEISURE -> Color(0xFFEAF0FD)
}
