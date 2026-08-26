package com.tteumsae.app.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.ui.common.formatMinutes
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed

@Composable
internal fun SavedPlaceDetailScreen(
    place: PlaceCandidate,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onOpenMap,
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
