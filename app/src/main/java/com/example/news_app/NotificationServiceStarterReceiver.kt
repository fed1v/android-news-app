package com.example.news_app

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context


class NotificationServiceStarterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        AlarmReceiver.setAlarm(context!!)
    }
}