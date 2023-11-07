package com.ibnzahoor98.geomute

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.databinding.ActivitySplashBinding


class Splash : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding  //defining the binding class
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {

            val intent = Intent(this@Splash, DoNotDisturbPermissionConsent::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
        }else{
            auth = Firebase.auth
            Glide.with(this).load(R.raw.logo).into(binding.imageView);
            createNotificationChannel()

            getUserInfo()
        }



    }



    fun countDowner(){
        object : CountDownTimer(Constants.TIME, Constants.INTERVAL){
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {

                openApp()
            }

        }.start();


    }

    fun openApp(){
        val intent = Intent(this@Splash, Main::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID_1,
                "GeoMute Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    fun getUserInfo (){
            var collection = ""
            var document = ""
            if (auth.currentUser == null)
            {
                var deviceId = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                collection = "guests"
                document = deviceId
            }else {

                collection = "users"
                document = auth.currentUser?.uid.toString()
            }
            db.collection(collection).document(document)
                .get()
                .addOnSuccessListener { documentReference ->
                    if (documentReference.exists())
                    {
                        val isAdRemoved = documentReference.data?.get("isAdRemoved");
                        val fenceLimit = documentReference.data?.get("fenceLimit")


                            try{
                                val geofence = documentReference.data?.get("geofences") as List<HashMap<String, *>>
                                SharedPrefs.getBasicInfoSharedPref(this).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_COUNT, geofence.size).apply()

                            }catch (e:Exception){
                                SharedPrefs.getBasicInfoSharedPref(this).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_COUNT, 0).apply()


                            }
                        SharedPrefs.getBasicInfoSharedPref(this).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_LIMIT, fenceLimit.toString().toInt()).apply()
                        SharedPrefs.getBasicInfoSharedPref(this).edit().putBoolean(SharedPrefs.SHARED_PREF_IS_AD_REMOVED, isAdRemoved as Boolean).apply()


                    }

                    countDowner()

                }
                .addOnFailureListener { e ->

                    countDowner()
                }


    }
}