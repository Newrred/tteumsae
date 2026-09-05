package com.tteumsae.app.ui.route

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed
import com.tteumsae.app.ui.theme.TteumRedSoft
import kotlinx.coroutines.delay

private val loadingSteps = listOf(
    "가는 길을 살펴보고 있어요",
    "들르기 좋은 장소를 비교하고 있어요",
    "늦지 않을 시간을 계산하고 있어요",
)

@Composable
internal fun RouteLoadingScreen(
    completed: Boolean,
    onBack: () -> Unit,
    background: @Composable (Modifier) -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    var activeStep by remember { mutableIntStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.95f,
        animationSpec = tween(220),
        label = "loading-card-scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(180),
        label = "loading-card-alpha",
    )
    LaunchedEffect(Unit) { entered = true }
    LaunchedEffect(completed) {
        if (completed) {
            activeStep = loadingSteps.size
        } else {
            activeStep = 0
            repeat(loadingSteps.lastIndex) {
                delay(700)
                activeStep += 1
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        background(Modifier.fillMaxSize())
    }
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.52f)),
        ) {
            val cardMaxHeight = (maxHeight - 48.dp).coerceAtLeast(240.dp)
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .heightIn(max = cardMaxHeight)
                    .semantics {
                        paneTitle = "경로 검색 진행 상황"
                        liveRegion = LiveRegionMode.Polite
                        stateDescription = if (completed) {
                            "검색 완료"
                        } else {
                            loadingSteps[activeStep.coerceIn(loadingSteps.indices)]
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 24.dp,
                border = BorderStroke(1.dp, Color(0xFFE7E8EC)),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 26.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        color = TteumRedSoft,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Crossfade(
                                targetState = completed,
                                animationSpec = tween(260),
                                label = "loading-complete-icon",
                            ) { isCompleted ->
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Map,
                                    contentDescription = null,
                                    tint = TteumRed,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (completed) "틈새를 찾았어요" else "가는 길 주변을 찾고 있어요",
                        color = TteumInk,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        loadingSteps.forEachIndexed { index, label ->
                            LoadingStepRow(
                                label = label,
                                complete = completed || index < activeStep,
                                active = !completed && index == activeStep,
                            )
                        }
                    }
                    if (!completed) {
                        Spacer(Modifier.height(10.dp))
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("검색 취소")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingStepRow(
    label: String,
    complete: Boolean,
    active: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            when {
                complete -> Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = TteumRed,
                    modifier = Modifier.size(18.dp),
                )
                active -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TteumRed,
                    strokeWidth = 2.dp,
                )
                else -> Surface(
                    modifier = Modifier.size(7.dp),
                    color = Color(0xFFD4D7DD),
                    shape = CircleShape,
                ) {}
            }
        }
        Text(
            label,
            modifier = Modifier.padding(start = 10.dp),
            color = if (active || complete) TteumInk else TteumMuted.copy(alpha = 0.72f),
            fontSize = 14.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
