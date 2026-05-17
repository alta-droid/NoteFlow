package com.noteflow.app

import android.app.Application
import com.noteflow.app.notification.NotificationHelper
import com.tomroush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        NotificationHelper.createNotificationChannel(this)
    }
}
