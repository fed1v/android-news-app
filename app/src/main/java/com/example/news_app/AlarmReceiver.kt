package com.example.news_app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class AlarmReceiver() : BroadcastReceiver() {
    private var imageBitmap: Bitmap? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, LoginActivity::class.java)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, i, 0)

        val title = intent!!.getStringExtra("notification_title")
        val description = intent.getStringExtra("notification_description")
        val urlToImage = intent.getStringExtra("urlToImage")

        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                imageBitmap = bitmap
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }

        if (urlToImage != null && urlToImage != "") {
            Picasso
                .get()
                .load(urlToImage)
                .resize(2048, 1600)
                .onlyScaleDown()
                .into(target)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context!!, "NewsApp")
            .setSmallIcon(R.drawable.ic_google) // TODO change
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        if(imageBitmap != null){
            builder
                .setLargeIcon(imageBitmap)
                .setStyle(
                    NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(imageBitmap)  // TODO null when app closed
                    .bigLargeIcon(null)
            )
        }

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(123, builder.build())
    }
}