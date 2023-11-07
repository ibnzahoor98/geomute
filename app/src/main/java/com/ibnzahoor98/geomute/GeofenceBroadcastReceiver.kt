package com.ibnzahoor98.geomute

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.ibnzahoor98.geomute.helper.Notification

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    @Override
    public override fun onReceive(context: Context, intent: Intent) {


        val geofencingEvent: GeofencingEvent? = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode())
            return;
        }

        val geofenceTransition = geofencingEvent?.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
            )
            {

                val triggeringGeofences: MutableList<Geofence>? = geofencingEvent?.getTriggeringGeofences();
                val locId = triggeringGeofences?.get(0)?.getRequestId();

                val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

//                val mNotificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
//                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

            }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {

            val triggeringGeofences: MutableList<Geofence>? = geofencingEvent?.getTriggeringGeofences();
            val locId = triggeringGeofences?.get(0)?.getRequestId();
            val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        }

        //
//            val audioManager = this.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
//            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
//
//            // To set full volume
//
//            // To set full volume
//            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
//            audioManager.setStreamVolume(
//                AudioManager.STREAM_RING,
//                maxVolume,
//                AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND
//            )



    }


}
