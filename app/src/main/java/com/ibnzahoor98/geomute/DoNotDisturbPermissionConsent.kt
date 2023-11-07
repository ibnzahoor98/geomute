package com.ibnzahoor98.geomute

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.databinding.ActivityDoNotDisturbPermissionConsentBinding
import com.ibnzahoor98.geomute.databinding.ActivityLocationPermissionConsentBinding

class DoNotDisturbPermissionConsent : AppCompatActivity() {

    private lateinit var binding: ActivityDoNotDisturbPermissionConsentBinding
    val db = Firebase.firestore
    var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoNotDisturbPermissionConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.grant.setOnClickListener({

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! notificationManager.isNotificationPolicyAccessGranted) {

                val intent = Intent(
                    Settings
                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

                startActivity(intent)
            }
            else {

                val intent = Intent(this@DoNotDisturbPermissionConsent, Splash::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent)
            }
        })

    }

    override fun onResume() {
        super.onResume()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! notificationManager.isNotificationPolicyAccessGranted) {


        }
        else {

            val intent = Intent(this@DoNotDisturbPermissionConsent, Splash::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
        }
    }
}