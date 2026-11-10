package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.service.MatrixMediaPlaybackService

class MatrixMusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"

        var currentTitle: String = "Snooze"
        var currentArtist: String = "SZA"
        var isPlaying: Boolean = false
        var currentPositionMs: Long = 80000L
        var durationMs: Long = 281000L

        fun updateAllWidgets(
            context: Context,
            title: String?,
            artist: String?,
            playing: Boolean,
            posMs: Long,
            durMs: Long
        ) {
            currentTitle = title ?: "Snooze"
            currentArtist = artist ?: "SZA"
            isPlaying = playing
            currentPositionMs = posMs
            durationMs = if (durMs > 0) durMs else 281000L

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MatrixMusicWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_matrix_music)

            views.setTextViewText(R.id.widget_track_title, currentTitle)
            views.setTextViewText(R.id.widget_track_artist, currentArtist)

            val curMin = currentPositionMs / 60000
            val curSec = (currentPositionMs % 60000) / 1000
            val durMin = durationMs / 60000
            val durSec = (durationMs % 60000) / 1000
            views.setTextViewText(R.id.widget_time_current, String.format("%02d:%02d", curMin, curSec))
            views.setTextViewText(R.id.widget_time_total, String.format("%02d:%02d", durMin, durSec))

            val progressPercent = if (durationMs > 0) ((currentPositionMs * 100) / durationMs).toInt().coerceIn(0, 100) else 35
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)

            views.setImageViewResource(
                R.id.widget_btn_play_pause,
                if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            )

            // Intent to open MainActivity on click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Action Pending Intents
            val playPauseIntent = Intent(context, MatrixMusicWidgetProvider::class.java).apply {
                action = ACTION_PLAY_PAUSE
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                PendingIntent.getBroadcast(context, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val prevIntent = Intent(context, MatrixMusicWidgetProvider::class.java).apply {
                action = ACTION_PREVIOUS
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_prev,
                PendingIntent.getBroadcast(context, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val nextIntent = Intent(context, MatrixMusicWidgetProvider::class.java).apply {
                action = ACTION_NEXT
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                PendingIntent.getBroadcast(context, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val serviceIntent = Intent(context, MatrixMediaPlaybackService::class.java)

        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                serviceIntent.action = MatrixMediaPlaybackService.ACTION_PLAY_PAUSE
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ACTION_PREVIOUS -> {
                serviceIntent.action = MatrixMediaPlaybackService.ACTION_PREVIOUS
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ACTION_NEXT -> {
                serviceIntent.action = MatrixMediaPlaybackService.ACTION_NEXT
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
