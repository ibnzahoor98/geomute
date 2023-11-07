package com.ibnzahoor98.geomute.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ibnzahoor98.geomute.Constants
import com.ibnzahoor98.geomute.R

class Notification {

    companion object{
        fun send(title:String, text:String, context: Context) {
            var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID_1)
                .setSmallIcon(R.drawable.logo_straight)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
            var notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManager.notify(1, builder.build());
        }

    }
}