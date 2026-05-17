package com.noteflow.app

import android.app.Application
import com.noteflow.app.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
