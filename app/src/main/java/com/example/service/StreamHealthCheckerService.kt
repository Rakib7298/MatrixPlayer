package com.example.service

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
import com.example.media.streaming.LinkHealthCheckerEngine
import com.example.media.streaming.StreamHealth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class StreamHealthCheckerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val isRunning = AtomicBoolean(false)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_CHECK) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!isRunning.getAndSet(true)) {
            createNotificationChannel(this)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Matrix Stream Link Monitor")
                .setContentText("Monitoring stream link health in background...")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            serviceScope.launch {
                val engine = LinkHealthCheckerEngine(applicationContext)
                while (isRunning.get()) {
                    val defaultEndpoints = engine.defaultEndpointsToTest.map { it.url to it.name }
                    val report = engine.runBatchScan(defaultEndpoints)

                    val deadCount = report.results.count { !it.isReachable || it.healthStatus == StreamHealth.OFFLINE }
                    if (deadCount > 0) {
                        notifyDeadLinksDetected(applicationContext, deadCount, report.results.size)
                    }

                    // Check every 30 minutes in background (or 1 minute for demo)
                    delay(30 * 60 * 1000L)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning.set(false)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "matrix_stream_health_channel"
        const val NOTIFICATION_ID = 2001
        const val DEAD_LINKS_NOTIF_ID = 2002
        const val ACTION_START_CHECK = "com.example.action.START_STREAM_HEALTH_CHECK"
        const val ACTION_STOP_CHECK = "com.example.action.STOP_STREAM_HEALTH_CHECK"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Matrix Stream Health Monitor",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts when M3U/M3U8 stream links become offline or unreachable"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        fun notifyDeadLinksDetected(context: Context, deadCount: Int, totalTested: Int) {
            createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("⚠️ Stream Health Alert: Dead Links Found")
                .setContentText("Detected $deadCount unreachable/offline stream link(s) out of $totalTested tested.")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(DEAD_LINKS_NOTIF_ID, notification)
        }

        fun startService(context: Context) {
            val intent = Intent(context, StreamHealthCheckerService::class.java).apply {
                action = ACTION_START_CHECK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
