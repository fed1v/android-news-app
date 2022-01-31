package com.example.news_app

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

import android.app.PendingIntent

import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import androidx.core.app.NotificationCompat
import androidx.legacy.content.WakefulBroadcastReceiver

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.NewsHeadlines
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target


class NotificationIntentService : IntentService(NotificationIntentService::class.java.simpleName) {
    companion object {
        private val ACTION_START = "ACTION_START"
        private val ACTION_DELETE = "ACTION_DELETE"

        fun createIntentStartNotificationService(context: Context?): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_START
            return intent
        }

        fun createIntentDeleteNotification(context: Context?): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_DELETE
            return intent
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onHandleIntent(p0: Intent?) {
        val intent = p0
        println("onHandleIntent, started handling a notification event")
        try {
            val action: String? = intent?.getAction()
            println("Action: $action")
            if (ACTION_START.equals(action)) {
                processStartNotification()
            }
            if (Intent.ACTION_DELETE == action) {
                processDeleteNotification()
            }
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent)
        }

    }

    private fun processStartNotification() {
        Log.v("Notifications", "processStartNotification")
        getNewsAndShow()
    }

    private fun processDeleteNotification() {
        Log.d("Notifications", "Notification deleted")
    }

    private fun getNewsAndShow() {
        val manager = RequestManager(this)
        manager.getNewsHeadlines(listener, null, null, null, "us")
    }

    private fun showNewsInNotifications(newsHeadlines: List<NewsHeadlines>) {
        val i = Intent(this, LoginActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT)

        val title = newsHeadlines[0].title
        val description = newsHeadlines[0].description
        val urlToImage = newsHeadlines[0].urlToImage

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "NewsApp")

        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    builder
                        .setLargeIcon(bitmap)
                        .setStyle(
                            NotificationCompat
                                .BigPictureStyle()
                                .bigPicture(bitmap)  // TODO null when app closed
                                .bigLargeIcon(null)
                        )
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        if (urlToImage != null && urlToImage != "") {
            try {
                Picasso.setSingletonInstance(Picasso.Builder(this).build())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Picasso
                .get()
                .load(urlToImage)
                .resize(2048, 1600)
                .onlyScaleDown()
                .into(target)
        }

        builder.setSmallIcon(R.drawable.ic_news)
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(AlarmReceiver.getDeleteIntent(this))
            .setVisibility(VISIBILITY_PUBLIC)
            .setTimeoutAfter(-1)

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(0, builder.build())
    }

    val listener: OnFetchDataListener<NewsApiResponse> =
        object : OnFetchDataListener<NewsApiResponse> {
            override fun onFetchData(newsHeadlinesList: List<NewsHeadlines>, message: String) {
                if (newsHeadlinesList.isNotEmpty()) {
                    showNewsInNotifications(newsHeadlinesList)
                }
            }

            override fun onError(message: String) {}
        }
}