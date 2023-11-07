package com.ibnzahoor98.geomute.fragment.dashboard

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.SharedPrefs
import com.ibnzahoor98.geomute.databinding.ActivityFenceDetailViewBinding
import com.ibnzahoor98.geomute.helper.Ads

class FenceDetailView : AppCompatActivity(), OnMapReadyCallback , View.OnClickListener  {

    private lateinit var binding: ActivityFenceDetailViewBinding
    private lateinit var mMap: GoogleMap
    private var auth = Firebase.auth
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFenceDetailViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapview) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.delete.setOnClickListener(this)
        binding.titleValue.text = intent.getStringExtra("name")
        binding.radiusValue.text = intent.getStringExtra("radius")
        binding.tagValue.text = intent.getStringExtra("tag")
        binding.timestampValue.text = convertDate(intent.getStringExtra("timestamp").toString(), "dd/MM/yyyy hh:mm:ss")
        binding.latValue.text = intent.getStringExtra("latitude")
        binding.longValue.text = intent.getStringExtra("longitude")
        binding.idValue.text = intent.getStringExtra("fenceId")
      //  binding.muteModeValue.text = intent.getStringExtra("muteMode")

        if (!SharedPrefs.isAdRemoved(this))
        {

            binding.adViewDetailFenceView.visibility = View.VISIBLE
            Ads.requestMainActivityAd(binding.adViewDetailFenceView, this)
            adFenceDetailviewActivityListener()

        }else{

        }

    }

    fun adFenceDetailviewActivityListener(){
        binding.adViewDetailFenceView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }
    fun convertDate(dateInMilliseconds: String, dateFormat: String?): String? {
        return DateFormat.format(dateFormat, dateInMilliseconds.toLong()).toString()
    }
    override fun onMapReady(googleMap: GoogleMap) {
        val getLat = intent.getStringExtra("latitude")!!.toDouble()
        val getLong = intent.getStringExtra("longitude")!!.toDouble()
        mMap = googleMap
        val coordinates = LatLng(getLat, getLong)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates))
        val circleOptions = CircleOptions()
            .center(coordinates)
            .radius(100.0) // In meters
            .strokeWidth(3f)
            .strokeColor(Color.parseColor("#FF3D48"))
            .fillColor(Color.parseColor("#99FF3D48"))
            .clickable(true)
        mMap.addCircle(circleOptions)


    }

    fun initDeleteFirebaseFenceRecord(){
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


        val docRef = db.collection(collection).document(document)


        docRef.update("geofences."+"0" , FieldValue.delete())
              .addOnSuccessListener {
                 // finish()

              }
            .addOnFailureListener { e ->

                //finish()
                Log.e("aaaaaaa", e.toString())

            }

    }
    fun initGeoFence(id:String){

    }
    override fun onClick(p0: View?) {

        if (p0?.id == R.id.delete)
        {

            binding.delete.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            initDeleteFirebaseFenceRecord()
            deleteFence(binding.idValue.text.toString())



        }

    }

    fun deleteFence(id:String){
        var geofencingClient = LocationServices.getGeofencingClient(this)
        var geoFence = ArrayList<String>()
        geoFence.add(id)
        geofencingClient.removeGeofences(geoFence)
            .addOnSuccessListener(this) { aVoid: Void? ->

            }
            .addOnFailureListener(this) { e: java.lang.Exception? ->
                Log.e("WHEN REMOVE", "Failed to remove geo fence: " + e?.localizedMessage)

            }
    }
}