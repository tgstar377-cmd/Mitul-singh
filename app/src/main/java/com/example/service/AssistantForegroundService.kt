package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ai.AssistantAction
import com.example.ui.VoiceTriggerActivity
import com.example.util.IntentExecutor

class AssistantForegroundService : Service {

    constructor() : super()

    companion object {
        const val CHANNEL_ID = "tg_assistant_foreground_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.START"
        const val ACTION_STOP = "com.example.service.STOP"
        const val ACTION_OPEN_YOUTUBE = "com.example.service.OPEN_YOUTUBE"
        const val ACTION_OPEN_WHATSAPP = "com.example.service.OPEN_WHATSAPP"

        fun startService(context: Context) {
            val intent = Intent(context, AssistantForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AssistantForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_OPEN_YOUTUBE -> {
                IntentExecutor.execute(
                    this,
                    AssistantAction.LaunchApp("YouTube", "com.google.android.youtube", "https://youtube.com")
                )
            }
            ACTION_OPEN_WHATSAPP -> {
                IntentExecutor.execute(
                    this,
                    AssistantAction.LaunchApp("WhatsApp", "com.whatsapp", "https://api.whatsapp.com")
                )
            }
            else -> {
                try {
                    val notification = buildNotification()
                    startForeground(NOTIFICATION_ID, notification)
                } catch (e: Exception) {
                    android.util.Log.e("AssistantFGS", "Failed to startForeground: ${e.message}", e)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        // Tap notification to open full TG Assistant
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick Voice Trigger Floating Overlay
        val voiceTriggerIntent = Intent(this, VoiceTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val voiceTriggerPendingIntent = PendingIntent.getActivity(
            this,
            1,
            voiceTriggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick YouTube
        val ytIntent = Intent(this, AssistantForegroundService::class.java).apply {
            action = ACTION_OPEN_YOUTUBE
        }
        val ytPendingIntent = PendingIntent.getService(
            this,
            2,
            ytIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick WhatsApp
        val waIntent = Intent(this, AssistantForegroundService::class.java).apply {
            action = ACTION_OPEN_WHATSAPP
        }
        val waPendingIntent = PendingIntent.getService(
            this,
            3,
            waIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TG Assistant Active • Mitul")
            .setContentText("Say \"TGA YouTube khol do\" or tap mic to command")
            .setSmallIcon(R.drawable.ic_tg_assistant)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_tg_assistant, "🎙️ Speak to TGA", voiceTriggerPendingIntent)
            .addAction(R.drawable.ic_tg_assistant, "🎬 YouTube", ytPendingIntent)
            .addAction(R.drawable.ic_tg_assistant, "💬 WhatsApp", waPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TG Assistant Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps TG Assistant ready to listen and execute commands from any screen."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
