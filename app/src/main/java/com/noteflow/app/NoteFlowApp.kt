package com.noteflow.app

import android.app.Application
import com.noteflow.app.notification.NotificationHelper
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        NotificationHelper.createNotificationChannel(this)
    }
}
