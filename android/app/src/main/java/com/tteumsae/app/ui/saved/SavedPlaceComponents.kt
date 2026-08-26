package com.tteumsae.app.ui.saved

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.platform.savedImageCache
import com.tteumsae.app.ui.common.compactTags
import com.tteumsae.app.ui.common.formatMinutes
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
internal fun SavedFilterChip(
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
internal fun SavedPlaceCard(
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
internal fun SavedPlaceImage(
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
