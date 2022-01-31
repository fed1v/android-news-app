package com.example.news_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.legacy.content.WakefulBroadcastReceiver
import java.util.*

class AlarmReceiver : WakefulBroadcastReceiver() {
    companion object {
        var calendar: Calendar? = null
        private val ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE"
        private val ACTION_DELETE_NOTIFICATION = "ACTION_DELETE_NOTIFICATION"

        fun setAlarm(context: Context) {
            calendar = Calendar.getInstance()
            val timeInMillis = context
                .getSharedPreferences("Notifications time", MODE_PRIVATE)!!
                .getLong("Time", -1L)
            calendar!!.timeInMillis = timeInMillis

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getStartPendingIntent(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar!!.timeInMillis,
                    pendingIntent
                )
            }
            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        }

        fun cancelAlarm(context: Context){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context.applicationContext, NotificationIntentService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
        }

        private fun getStartPendingIntent(context: Context): PendingIntent? {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = ACTION_START_NOTIFICATION_SERVICE
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun getDeleteIntent(context: Context?): PendingIntent? {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = ACTION_DELETE_NOTIFICATION
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        var serviceIntent: Intent? = null
        if (ACTION_START_NOTIFICATION_SERVICE == action) {
            serviceIntent = NotificationIntentService.createIntentStartNotificationService(context)
        } else if (ACTION_DELETE_NOTIFICATION == action) {
            Log.i(
                javaClass.simpleName,
                "onReceive delete notification action, starting notification service to handle delete"
            )
            serviceIntent = NotificationIntentService.createIntentDeleteNotification(context)
        }
        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent)
        }
    }
}