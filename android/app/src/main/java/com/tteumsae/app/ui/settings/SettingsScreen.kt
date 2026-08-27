package com.tteumsae.app.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.tteumsae.app.ui.BottomNavigation
import com.tteumsae.app.ui.account.AccountDeletionDialogs
import com.tteumsae.app.ui.account.AccountSettingsCard
import com.tteumsae.app.ui.account.AccountUiState
import com.tteumsae.app.ui.account.LoginSheet
import com.tteumsae.app.ui.navigation.MainTab
import com.tteumsae.app.ui.theme.TteumMuted
import com.tteumsae.app.ui.theme.TteumRed

@Composable
internal fun SettingsScreen(
    accountState: AccountUiState,
    savedCount: Int,
    locationPermissionGranted: Boolean,
    kakaoMapAvailable: Boolean,
    appVersion: String,
    contactEmail: String,
    privacyPolicyAvailable: Boolean,
    locationTermsAvailable: Boolean,
    onOpenLogin: () -> Unit,
    onDismissLogin: () -> Unit,
    onLogin: (com.tteumsae.app.domain.account.LoginProvider) -> Unit,
    onOpenProfile: () -> Unit,
    onSignOut: () -> Unit,
    onRequestAccountDeletion: () -> Unit,
    onConfirmDeletionConsequences: () -> Unit,
    onRequireReauthentication: () -> Unit,
    onReauthenticateForDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenKakaoMap: () -> Unit,
    onClearCache: () -> Boolean,
    onClearSaved: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenLocationTerms: () -> Unit,
    onContact: () -> Unit,
    onShowMessage: (String) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    var showSavedClearDialog by remember { mutableStateOf(false) }
    var showCacheClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        bottomBar = {
            BottomNavigation(
                selectedTab = MainTab.SETTINGS,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("설정", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                SettingsSectionTitle("계정")
                AccountSettingsCard(
                    state = accountState,
                    onLogin = onOpenLogin,
                    onOpenProfile = onOpenProfile,
                    onSignOut = onSignOut,
                    onDeleteAccount = onRequestAccountDeletion,
                )

                Spacer(Modifier.height(24.dp))

                SettingsSectionTitle("앱 사용")
                SettingsGroup {
                    SettingsRow(
                        title = "위치 권한",
                        description = if (locationPermissionGranted) "허용됨" else "허용되지 않음",
                        onClick = onOpenLocationSettings,
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "카카오맵",
                        description = if (kakaoMapAvailable) "설치됨 · 눌러서 실행" else "설치 필요",
                        onClick = onOpenKakaoMap,
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("저장 공간")
                SettingsGroup {
                    SettingsRow(
                        title = "캐시 지우기",
                        description = "임시 이미지와 지도 데이터를 정리해요.",
                        onClick = { showCacheClearDialog = true },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "저장한 장소 비우기",
                        description = guestSavedStorageDescription(savedCount),
                        titleColor = if (savedCount > 0) TteumRed else TteumMuted,
                        onClick = if (savedCount > 0) {
                            { showSavedClearDialog = true }
                        } else {
                            null
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("약관 및 지원")
                SettingsGroup {
                    SettingsRow(
                        title = "개인정보처리방침",
                        description = if (privacyPolicyAvailable) "보기" else "준비 중",
                        onClick = onOpenPrivacyPolicy,
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "위치기반서비스 이용약관",
                        description = if (locationTermsAvailable) "보기" else "준비 중",
                        onClick = onOpenLocationTerms,
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "문의하기",
                        description = contactEmail,
                        onClick = onContact,
                    )
                }

                Spacer(Modifier.height(24.dp))
                SettingsSectionTitle("앱 정보")
                SettingsGroup {
                    SettingsRow(
                        title = "앱 버전",
                        description = appVersion,
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "데이터 출처",
                        description = "한국관광공사 TourAPI · 카카오맵",
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (accountState.showLoginSheet) {
        LoginSheet(onDismiss = onDismissLogin, onLogin = onLogin)
    }

    AccountDeletionDialogs(
        step = accountState.deletionStep,
        isLoading = accountState.isLoading,
        onContinue = onConfirmDeletionConsequences,
        onRequireReauthentication = onRequireReauthentication,
        onReauthenticate = onReauthenticateForDeletion,
        onCancel = onCancelDeletion,
    )

    if (showSavedClearDialog) {
        AlertDialog(
            onDismissRequest = { showSavedClearDialog = false },
            title = { Text("저장 목록을 비울까요?") },
            text = { Text("이 기기에 저장한 장소가 모두 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearSaved()
                        showSavedClearDialog = false
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavedClearDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    if (showCacheClearDialog) {
        AlertDialog(
            onDismissRequest = { showCacheClearDialog = false },
            title = { Text("캐시를 지울까요?") },
            text = { Text("저장한 장소는 유지되고 임시 데이터만 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleared = onClearCache()
                        showCacheClearDialog = false
                        onShowMessage(
                            if (cleared) "캐시를 정리했어요." else "캐시를 정리하지 못했어요.",
                        )
                    },
                ) {
                    Text("지우기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCacheClearDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

internal fun guestSavedStorageDescription(count: Int): String =
    "이 기기에 ${count}개 저장됨"
