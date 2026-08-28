package com.tteumsae.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tteumsae.app.R

object ReminderNotifications {
    private const val CHANNEL_ID = "departure_reminders"
    private const val NOTIFICATION_ID = 4105

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "출발 시간 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "선택한 경유지에서 출발해야 할 시각을 알려드려요."
            },
        )
    }

    fun show(context: Context, trip: ActiveTrip) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val navigationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trip.navigationUrl))
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            navigationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("이제 출발할 시간이에요")
            .setContentText("${trip.stopName}에서 출발해 목적지로 이동해 주세요.")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }
}
