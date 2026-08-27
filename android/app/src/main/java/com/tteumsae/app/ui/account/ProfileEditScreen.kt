package com.tteumsae.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.account.AgeGroup
import com.tteumsae.app.domain.account.Gender
import com.tteumsae.app.domain.account.UserProfile
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileEditScreen(
    profile: UserProfile,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (String?, AgeGroup?, Gender?) -> Unit,
) {
    var displayName by remember(profile.userId, profile.displayName) { mutableStateOf(profile.displayName.orEmpty()) }
    var ageGroup by remember(profile.userId, profile.ageGroup) { mutableStateOf(profile.ageGroup) }
    var gender by remember(profile.userId, profile.gender) { mutableStateOf(profile.gender) }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            TopAppBar(
                title = { Text("프로필 관리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("기본 정보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = displayName,
                onValueChange = { if (it.length <= 40) displayName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("닉네임") },
                supportingText = { Text("선택 · 최대 40자") },
                singleLine = true,
            )
            OptionalChoice(
                label = "연령대",
                selectedLabel = ageGroup?.label() ?: "선택하지 않음",
                options = listOf(null) + AgeGroup.entries,
                optionLabel = { it?.label() ?: "선택하지 않음" },
                onSelected = { ageGroup = it },
            )
            OptionalChoice(
                label = "성별",
                selectedLabel = gender?.label() ?: "선택하지 않음",
                options = listOf(null) + Gender.entries,
                optionLabel = { it?.label() ?: "선택하지 않음" },
                onSelected = { gender = it },
            )
            Text(
                "연령대와 성별은 선택사항이며 현재 장소 추천 결과에는 사용하지 않습니다.",
                color = TteumMuted,
                fontSize = 13.sp,
            )
            errorMessage?.let { Text(it, color = TteumRed, fontSize = 13.sp) }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onSave(displayName, ageGroup, gender) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            ) {
                Text(if (isLoading) "저장 중…" else "저장")
            }
        }
    }
}

@Composable
private fun <T> OptionalChoice(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedLabel)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private fun AgeGroup.label(): String = when (this) {
    AgeGroup.UNDER_20 -> "20세 미만"
    AgeGroup.TWENTIES -> "20대"
    AgeGroup.THIRTIES -> "30대"
    AgeGroup.FORTIES -> "40대"
    AgeGroup.FIFTIES -> "50대"
    AgeGroup.SIXTY_PLUS -> "60대 이상"
    AgeGroup.PREFER_NOT_TO_SAY -> "응답하지 않음"
}

private fun Gender.label(): String = when (this) {
    Gender.FEMALE -> "여성"
    Gender.MALE -> "남성"
    Gender.OTHER -> "기타"
    Gender.PREFER_NOT_TO_SAY -> "응답하지 않음"
}
