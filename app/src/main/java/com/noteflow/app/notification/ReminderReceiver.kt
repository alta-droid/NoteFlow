package com.noteflow.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("note_id", -1L)
        val title = intent.getStringExtra("note_title") ?: "تذكير"
        val content = intent.getStringExtra("note_content") ?: ""

        if (noteId != -1L) {
            NotificationHelper.showReminderNotification(context, noteId, title, content)
        }
    }
}
