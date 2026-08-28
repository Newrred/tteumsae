package com.tteumsae.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tteumsae.app.domain.account.LoginProvider
import com.tteumsae.app.ui.theme.TteumInk
import com.tteumsae.app.ui.theme.TteumMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginSheet(
    onDismiss: () -> Unit,
    onLogin: (LoginProvider) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(LOGIN_SHEET_TITLE, color = TteumInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                LOGIN_SHEET_DESCRIPTION,
                color = TteumMuted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onLogin(LoginProvider.KAKAO) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE500),
                    contentColor = Color(0xFF191919),
                ),
            ) {
                Text("카카오로 계속하기", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onLogin(LoginProvider.GOOGLE) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Google로 계속하기", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
