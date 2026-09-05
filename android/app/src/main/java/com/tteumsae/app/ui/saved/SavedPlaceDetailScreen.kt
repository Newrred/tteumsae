package com.tteumsae.app.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.ui.route.normalizedVisitInfo
import com.tteumsae.app.ui.route.plainTourText
import com.tteumsae.app.ui.route.practicalVisitFacts
import com.tteumsae.app.ui.route.placeSourceCaption
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft

private val SavedDetailMaxWidth = 720.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SavedPlaceDetailScreen(
    place: PlaceCandidate,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val address = normalizedVisitInfo(place.address)
    val overview = plainTourText(place.overview)
    val visitFacts = practicalVisitFacts(place)
    val tags = place.tags.mapNotNull(::normalizedVisitInfo).distinct()
    val heroImageUrl = normalizedVisitInfo(place.imageUrl)
        ?: place.imageUrls.firstNotNullOfOrNull(::normalizedVisitInfo)
        ?: ""
    val heroHeight = if (heroImageUrl.isBlank()) 164.dp else 232.dp

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .widthIn(max = SavedDetailMaxWidth)
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TteumRed),
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("카카오맵에서 보기", fontWeight = FontWeight.Bold)
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = SavedDetailMaxWidth)
                        .fillMaxWidth()
                        .height(heroHeight),
                ) {
                    SavedPlaceImage(
                        imageUrl = heroImageUrl,
                        category = place.category,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(
                                Color.White.copy(alpha = 0.94f),
                                CircleShape,
                            ),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.background(
                                Color.White.copy(alpha = 0.94f),
                                CircleShape,
                            ),
                        ) {
                            Icon(
                                if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isSaved) "저장 해제" else "저장",
                                tint = if (isSaved) TteumRed else TteumMuted,
                            )
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = SavedDetailMaxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                ) {
                    Surface(
                        color = TteumRedSoft,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            place.category.label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = TteumRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        place.name,
                        color = TteumInk,
                        fontSize = 27.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    address?.let {
                        Spacer(Modifier.height(9.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = TteumMuted,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(18.dp),
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                it,
                                modifier = Modifier.weight(1f),
                                color = TteumMuted,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }

                    overview?.let {
                        Spacer(Modifier.height(24.dp))
                        SavedDetailSectionTitle("장소 소개")
                        Spacer(Modifier.height(9.dp))
                        Text(
                            it,
                            color = Color(0xFF4F5560),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        )
                    }

                    if (visitFacts.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        SavedDetailSectionTitle("방문 전 확인")
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            visitFacts.forEach { fact ->
                                SavedDetailInfoRow(label = fact.label, value = fact.value)
                            }
                        }
                    }

                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        SavedDetailSectionTitle("장소 특징")
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    color = Color(0xFFF1F2F4),
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Text(
                                        tag,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 7.dp,
                                        ),
                                        color = Color(0xFF55585F),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        placeSourceCaption(place),
                        color = TteumMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedDetailSectionTitle(text: String) {
    Text(
        text,
        color = TteumInk,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SavedDetailInfoRow(
    label: String,
    value: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7F8FA),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                label,
                modifier = Modifier.width(72.dp),
                color = TteumInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                value,
                modifier = Modifier.weight(1f),
                color = TteumMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}
