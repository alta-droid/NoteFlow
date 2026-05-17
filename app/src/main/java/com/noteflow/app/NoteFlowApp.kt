package com.noteflow.app

import android.app.Application
import com.noteflow.app.notification.NotificationHelper

class NoteFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
