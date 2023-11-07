package com.ibnzahoor98.geomute.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.Main
import com.ibnzahoor98.geomute.SharedPrefs
import com.ibnzahoor98.geomute.Splash
import kotlin.reflect.typeOf

class TransferGuestToUser {


    companion object{
        val db = Firebase.firestore
        var auth = Firebase.auth
        fun begin(context: Context){



            val deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            db.collection("guests").document(deviceId)
                .get()
                .addOnSuccessListener { documentReference ->
                    if (documentReference.exists())
                    {

                        val isAdRemoved = documentReference.data?.get("isAdRemoved")
                        var fenceLimit = documentReference.data!!.get("fenceLimit")
                        val geofences = documentReference.data!!.get("geofences")
                        val uid = auth.currentUser?.uid.toString()

                        var localFenceLimit: Long? = fenceLimit as Long
                        if (localFenceLimit == 2L )
                        {
                            localFenceLimit = 4

                        }

                        val docData = hashMapOf(
                            "isAdRemoved" to isAdRemoved,
                            "fenceLimit" to localFenceLimit,
                            "geofences" to geofences,
                            "uid" to uid,
                        )
                        db.collection("users")
                            .document(uid)
                            .set(docData)



                    }
                }
                .addOnFailureListener { e ->
 Log.e("fffff", e.localizedMessage)
                }



        }


    }

}