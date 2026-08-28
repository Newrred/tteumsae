package com.tteumsae.app.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun detourPinLabel(recommendation: SafeRecommendation): String =
    "+${recommendation.place.detourMinutes}분"

internal fun nextSelectedPlaceId(current: String?, tapped: String): String? =
    tapped.takeUnless { it == current }

internal fun maximumStayLabel(recommendation: SafeRecommendation): String =
    recommendation.maximumStayMinutes?.let { "이동 기준 최대 약 ${it}분" }
        ?: "체류 가능 시간을 다시 확인해 주세요"

internal fun latestDepartureLabel(
    recommendation: SafeRecommendation,
    zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
): String = recommendation.latestDepartureEpochMillis?.let { epochMillis ->
    val formatted = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("a h시 m분", Locale.KOREAN))
    "${formatted}까지 출발하면 돼요"
} ?: "출발 권장시각을 다시 확인해 주세요"

internal fun productNavigationRecommendations(
    selectedPlaceId: String?,
    recommendations: List<SafeRecommendation>,
): List<SafeRecommendation> = selectedPlaceId?.let { selected ->
    recommendations.firstOrNull { it.place.id == selected }?.let(::listOf)
}.orEmpty()

@Composable
internal fun RouteCandidateCard(
    recommendation: SafeRecommendation,
    selected: Boolean,
    onSelect: () -> Unit,
    onDetail: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(),
        border = if (selected) BorderStroke(2.dp, TteumRed) else null,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(recommendation.place.name, fontWeight = FontWeight.Bold)
                    Text(
                        "추가 이동 ${detourPinLabel(recommendation)}",
                        color = TteumMuted,
                    )
                }
                Text(if (selected) "선택됨" else "선택", color = TteumRed, fontWeight = FontWeight.Bold)
            }
            if (selected) {
                Spacer(Modifier.height(12.dp))
                Text(maximumStayLabel(recommendation), fontWeight = FontWeight.Bold)
                Text(latestDepartureLabel(recommendation), color = TteumRed)
            }
            TextButton(onClick = onDetail) { Text("상세보기") }
        }
    }
}
