package com.ibnzahoor98.geomute

import android.Manifest
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.data.UploadData
import com.ibnzahoor98.geomute.databinding.ActivityCreateGeoFenceBinding
import com.ibnzahoor98.geomute.fragment.dashboard.Dashboard
import com.ibnzahoor98.geomute.helper.CustomTypefaceSpan
import com.ibnzahoor98.geomute.helper.GenerateID
import com.ibnzahoor98.geomute.helper.Notification
import com.wooplr.spotlight.SpotlightView
import hearsilent.discreteslider.DiscreteSlider.OnValueChangedListener
import kotlinx.coroutines.flow.merge


class CreateGeoFence : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener, OnClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateGeoFenceBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var circleLocationSelected: LatLng
    private lateinit var newlyCreatedCircle: Circle
    val db = Firebase.firestore
    var auth = Firebase.auth
    private var fenceTag = ""
    lateinit var geofencingClient: GeofencingClient
    private val geofenceList: ArrayList<Geofence> = ArrayList()
    private var geofencePendingIntent: PendingIntent? = null
    private var fenceId:String = ""
    private var HIDE_ACTIVE_FENCES = false

    private final var TAG = "GeoFenceActivity"
    private lateinit var rewardedInterstitialAd:RewardedInterstitialAd

//    private var MUTE_MODE_DO_NOT_DISTURB = "1"
//    private var MUTE_MODE_VIBRATION = "2"

  //  private var MUTE_MODE = MUTE_MODE_VIBRATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateGeoFenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setCircleRadius()
//        binding.vibrate.setOnClickListener(this)
//        binding.donotDisturb.setOnClickListener(this)
        binding.hideShow.setOnClickListener(this)
        binding.create.setOnClickListener(this)
        binding.tag1.setOnClickListener(this)
        binding.tag2.setOnClickListener(this)
        binding.tag3.setOnClickListener(this)
        binding.tag4.setOnClickListener(this)
        binding.tag5.setOnClickListener(this)
        binding.tag6.setOnClickListener(this)
        binding.tag7.setOnClickListener(this)
        binding.tag8.setOnClickListener(this)
        binding.tag9.setOnClickListener(this)
        binding.tag10.setOnClickListener(this)
        binding.tag11.setOnClickListener(this)
        binding.tag12.setOnClickListener(this)
        binding.tag13.setOnClickListener(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
       // removeGeofence()
       // defaultMuteMode()
        defaultTagMod()
        spotlight()
    }
    fun spotlight(){
        object : CountDownTimer(1500, Constants.INTERVAL){
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {

                SpotlightView.Builder(this@CreateGeoFence)
                    .introAnimationDuration(200)
                    .enableRevealAnimation(false)
                    .performClick(true)
                    .fadeinTextDuration(200)
                    .headingTvColor(Color.parseColor("#ffffff"))
                    .headingTvSize(32)
                    .headingTvText("Long Press")
                    .subHeadingTvColor(Color.parseColor("#ffffff"))
                    .subHeadingTvSize(16)
                    .subHeadingTvText("Long press on map to create silent geo fence.")
                    .maskColor(Color.parseColor("#dc000000"))
                    .target(binding.info)
                    .lineAnimDuration(200)
                    .lineAndArcColor(Color.parseColor("#ffffff"))
                    .dismissOnTouch(true)
                    .dismissOnBackPress(true)
                    .enableDismissAfterShown(true)
                    .usageId("59") //UNIQUE ID
                    .setTypeface(ResourcesCompat.getFont(applicationContext, R.font.u_semi_bold))
                    .targetPadding(300)

                    .show()
            }

        }.start();



    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        displayAlreadyCreatedCircles()
        mMap.setOnMapLongClickListener(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location ->

                        val circleLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(circleLocation))



            }

    }


    fun hideAlreadyCreatedCircles(){
        mMap.clear()
        if (::circleLocationSelected.isInitialized)
        {
            mMap.addCircle(CircleOptions()
                .center(circleLocationSelected)
                .radius(binding.discreteSlider.progress+ 0.00) // In meters
                .strokeWidth(3f)
                .strokeColor(Color.parseColor("#FF3D48"))
                .fillColor(Color.parseColor("#99FF3D48"))
                .clickable(true))
        }


    }

    fun displayAlreadyCreatedCircles(){

        if (!HIDE_ACTIVE_FENCES)
        {
            if (Dashboard.STORE_CIRCLES != null && Dashboard.STORE_CIRCLES.size > 0)
            {
                for (i in Dashboard.STORE_CIRCLES)
                {

                    var circle = CircleOptions()
                        .center(LatLng(i.coordinates.latitude, i.coordinates.longitude))
                        .radius(i.radius) // In meters
                        .strokeWidth(3f)
                        .strokeColor(Color.parseColor("#344ED3"))
                        .fillColor(Color.parseColor("#99344ED3"))
                        .clickable(true)
                    mMap.addCircle(circle)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(i.coordinates.latitude, i.coordinates.longitude))
                            .title(i.name)
                            .snippet("Radius: " + i.radius + "m")
                    )?.showInfoWindow()
                }


            }
        }


    }

    override fun onMapLongClick(p0: LatLng) {

        mMap.clear()



        mMap.moveCamera(CameraUpdateFactory.newLatLng(p0))
        binding.latitude.text = "Lat: " + String.format("%4f", p0.latitude)
        binding.longitude.text = "  Long: " + String.format("%4f", p0.longitude)
        mMap.animateCamera(CameraUpdateFactory.scrollBy(0f, 150f));
        binding.createCircleCardView.visibility = View.VISIBLE
        circleLocationSelected = p0
        binding.discreteSlider.progress = 100
        val circleOptions = CircleOptions()
            .center(circleLocationSelected)
            .radius(100.0) // In meters
            .strokeWidth(3f)
            .strokeColor(Color.parseColor("#FF3D48"))
            .fillColor(Color.parseColor("#99FF3D48"))
            .clickable(true)
        newlyCreatedCircle = mMap.addCircle(circleOptions)

        displayAlreadyCreatedCircles()

    }

    fun updateCircleOnMap(radius:Double){
        newlyCreatedCircle.radius = radius
    }

    fun setCircleRadius()
    {
        binding.discreteSlider.setValueChangedImmediately(true); // Default is false
        binding.discreteSlider.setOnValueChangedListener(object : OnValueChangedListener() {
            override fun onValueChanged(progress: Int, fromUser: Boolean) {
                super.onValueChanged(progress, fromUser)

                updateCircleOnMap((progress+0.0))
                binding.diameter.setText("Diameter: " + (progress * 2)  +" m")
            }

            override fun onValueChanged(minProgress: Int, maxProgress: Int, fromUser: Boolean) {
                super.onValueChanged(minProgress, maxProgress, fromUser)

            }
        })
    }

    fun upload(){

        val timestamp = System.currentTimeMillis()

        val data =   UploadData(

            binding.name.text.toString(),
            binding.discreteSlider.progress + 0.00,
            fenceTag,
            timestamp,
            GeoPoint(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude),
            fenceId

        )
        var collection = ""
        var document = ""
        if (auth.currentUser == null)
        {
            var deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            collection = "guests"
            document = deviceId
        }else {

            collection = "users"
            document = auth.currentUser?.uid.toString()
        }


        db.collection(collection)
            .document(document)
            .update("geofences", FieldValue.arrayUnion(data))
            .addOnSuccessListener { documentReference ->
                binding.successful.visibility = View.VISIBLE;
                binding.createCircleCardView.visibility = View.GONE;
                binding.info.visibility = View.GONE
                binding.hideShow.visibility = View.GONE
                Glide.with(this).load(R.raw.success).into(binding.successImage);

                var currentCount = SharedPrefs.getFenceCount(this)
                SharedPrefs.getBasicInfoSharedPref(this).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_COUNT, currentCount+1).apply()

                Notification.send("Fence Created", "Your silent geo fence is created successfully", this)
//                MobileAds.initialize(this) { initializationStatus ->
//                    loadAd()
//                }

                object : CountDownTimer(3000, Constants.INTERVAL){
                    override fun onTick(p0: Long) {
                    }

                    override fun onFinish() {
                        finish()
                    }

                }.start();
            }
            .addOnFailureListener { e ->

                 Log.e("FENDEEE", e.localizedMessage)

            }


    }
//    private fun loadAd() {
//        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
//            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
//                override fun onAdLoaded(ad: RewardedInterstitialAd) {
//                    Log.d(TAG, "Ad was loaded.")
//                    rewardedInterstitialAd = ad
//                    rewardedInterstitialAd.fullScreenContentCallback = object: FullScreenContentCallback() {
//                        override fun onAdClicked() {
//                            // Called when a click is recorded for an ad.
//                            Log.d(TAG, "Ad was clicked.")
//                        }
//
//                        override fun onAdDismissedFullScreenContent() {
//                            // Called when ad is dismissed.
//                            // Set the ad reference to null so you don't show the ad a second time.
//                            Log.d(TAG, "Ad dismissed fullscreen content.")
//                          //  rewardedInterstitialAd = null
//                        }
//
//                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                            // Called when ad fails to show.
//                            Log.e(TAG, "Ad failed to show fullscreen content.")
//                           // rewardedInterstitialAd = null
//                        }
//
//                        override fun onAdImpression() {
//                            // Called when an impression is recorded for an ad.
//                            Log.d(TAG, "Ad recorded an impression.")
//                        }
//
//                        override fun onAdShowedFullScreenContent() {
//                            // Called when ad is shown.
//                            Log.d(TAG, "Ad showed fullscreen content.")
//                        }
//                    }
//                }
//
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//
//                   // rewardedInterstitialAd = null
//                }
//            })
//    }
//    override fun onUserEarnedReward(rewardItem: RewardItem) {
//        Log.d(TAG, "User earned reward.")
//        finish()
//    }
//

    fun createGeoFence(){
        fenceId  = GenerateID.create()!!
        geofenceList.add(
            Geofence.Builder()
                .setRequestId(fenceId)
                .setCircularRegion(
                    mMap.cameraPosition.target.latitude,
                    mMap.cameraPosition.target.longitude,
                    binding.discreteSlider.progress.toFloat()
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                 or Geofence.GEOFENCE_TRANSITION_DWELL)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setLoiteringDelay(1000)

                .build()
        )
    }
    private fun getGeofencingRequest(): GeofencingRequest? {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        builder.addGeofences(geofenceList)

        return builder.build()
    }

    private fun getGeofencePendingIntent(): PendingIntent? {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent
        }
         val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            geofencePendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            geofencePendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return geofencePendingIntent
    }

    private fun addGeofence() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
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
        geofencingClient.addGeofences(getGeofencingRequest()!!, getGeofencePendingIntent()!!)
            .addOnSuccessListener(this) { aVoid: Void? ->

            }
            .addOnFailureListener(this) { e: Exception? ->

            }
    }
    private  fun removeGeofence() {
        geofencingClient.removeGeofences(getGeofencePendingIntent()!!)
            .addOnSuccessListener(this) { aVoid: Void? ->

            }
            .addOnFailureListener(this) { e: java.lang.Exception? ->

            }

    }
//
//    fun defaultMuteMode(){
//        MUTE_MODE = MUTE_MODE_VIBRATION
//        binding.donotDisturb.background = ContextCompat.getDrawable(this, R.drawable.light_gray_rounded_bg)
//        binding.donotDisturb.isEnabled = true
//        ImageViewCompat.setImageTintList(binding.donotDisturb, ColorStateList.valueOf(Color.BLACK));
//
//        binding.vibrate.background = ContextCompat.getDrawable(this, R.drawable.blue_rounded_bg)
//        binding.vibrate.isEnabled = false
//        ImageViewCompat.setImageTintList(binding.vibrate, ColorStateList.valueOf(Color.WHITE));
//
//    }

    fun defaultTagMod(){

        fenceTag = binding.tag2.text.toString()
        binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag1.isEnabled = true
        binding.tag1.setTextColor(Color.BLACK)

        binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
        binding.tag2.isEnabled = false
        binding.tag2.setTextColor(Color.WHITE)

        binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag3.isEnabled = true
        binding.tag3.setTextColor(Color.BLACK)

        binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag4.isEnabled = true
        binding.tag4.setTextColor(Color.BLACK)

        binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag5.isEnabled = true
        binding.tag5.setTextColor(Color.BLACK)

        binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag6.isEnabled = true
        binding.tag6.setTextColor(Color.BLACK)

        binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag7.isEnabled = true
        binding.tag7.setTextColor(Color.BLACK)

        binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag8.isEnabled = true
        binding.tag8.setTextColor(Color.BLACK)

        binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag9.isEnabled = true
        binding.tag9.setTextColor(Color.BLACK)

        binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag10.isEnabled = true
        binding.tag10.setTextColor(Color.BLACK)

        binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag11.isEnabled = true
        binding.tag11.setTextColor(Color.BLACK)

        binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag12.isEnabled = true
        binding.tag12.setTextColor(Color.BLACK)

        binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
        binding.tag13.isEnabled = true
        binding.tag13.setTextColor(Color.BLACK)

    }

    override fun onClick(p0: View?) {

        if (p0?.id == R.id.hideShow)
        {
            if (!HIDE_ACTIVE_FENCES)
            {
                hideAlreadyCreatedCircles()
                HIDE_ACTIVE_FENCES = true
                binding.hideShowValue.text = "Show active fences"
            }else{

                HIDE_ACTIVE_FENCES = false
                displayAlreadyCreatedCircles()
                binding.hideShowValue.text = "Hide active fences"
            }
        }

//        if (p0?.id == R.id.donotDisturb)
//        {
//            MUTE_MODE = MUTE_MODE_DO_NOT_DISTURB
//
//            binding.donotDisturb.background = ContextCompat.getDrawable(this, R.drawable.blue_rounded_bg)
//            binding.donotDisturb.isEnabled = false
//            ImageViewCompat.setImageTintList(binding.donotDisturb, ColorStateList.valueOf(Color.WHITE));
//
//            binding.vibrate.background = ContextCompat.getDrawable(this, R.drawable.light_gray_rounded_bg)
//            binding.vibrate.isEnabled = true
//            ImageViewCompat.setImageTintList(binding.vibrate, ColorStateList.valueOf(Color.BLACK));
//
//            binding.muteModeChangeNotification.text = "Phone will be switch to 'DO NOT DISTURB' mode."
//            binding.muteModeChangeNotification.visibility = View.VISIBLE
//            object : CountDownTimer(1500, Constants.INTERVAL){
//                override fun onTick(p0: Long) {
//                }
//
//                override fun onFinish() {
//
//                    binding.muteModeChangeNotification.text = ""
//                    binding.muteModeChangeNotification.visibility = View.GONE
//                }
//
//            }.start();
//        }
//        if (p0?.id == R.id.vibrate)
//        {
//            MUTE_MODE = MUTE_MODE_VIBRATION
//            binding.donotDisturb.background = ContextCompat.getDrawable(this, R.drawable.light_gray_rounded_bg)
//            binding.donotDisturb.isEnabled = true
//            ImageViewCompat.setImageTintList(binding.donotDisturb, ColorStateList.valueOf(Color.BLACK));
//
//            binding.vibrate.background = ContextCompat.getDrawable(this, R.drawable.blue_rounded_bg)
//            binding.vibrate.isEnabled = false
//            ImageViewCompat.setImageTintList(binding.vibrate, ColorStateList.valueOf(Color.WHITE));
//
//
//            binding.muteModeChangeNotification.text = "Phone will be switch to 'VIBRATION' mode."
//            binding.muteModeChangeNotification.visibility = View.VISIBLE
//            object : CountDownTimer(1500, Constants.INTERVAL){
//                override fun onTick(p0: Long) {
//                }
//
//                override fun onFinish() {
//
//                    binding.muteModeChangeNotification.text = ""
//                    binding.muteModeChangeNotification.visibility = View.GONE
//                }
//
//            }.start();
//        }


        if (p0?.id == R.id.create)
        {
            if (binding.name.text.toString() == "")
            {


                val font = ResourcesCompat.getFont(applicationContext, R.font.u_semi_bold);

                val ssbuilder = SpannableStringBuilder("Please, enter your name!")
                ssbuilder.setSpan(
                    CustomTypefaceSpan("", font),
                    0,
                    ssbuilder.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )


                binding.name.setError(ssbuilder)
                binding.name.requestFocus()




            }
            else{
                binding.create.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                createGeoFence()
                addGeofence()
                upload()
            }

        }


        if (p0?.id == R.id.tag1){

            fenceTag = binding.tag1.text.toString()

            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag1.isEnabled = false
            binding.tag1.setTextColor(Color.WHITE)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag2){

            fenceTag = binding.tag2.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag2.isEnabled = false
            binding.tag2.setTextColor(Color.WHITE)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag3){

            fenceTag = binding.tag3.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag3.isEnabled = false
            binding.tag3.setTextColor(Color.WHITE)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag4){

            fenceTag = binding.tag4.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag4.isEnabled = false
            binding.tag4.setTextColor(Color.WHITE)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag5){

            fenceTag = binding.tag5.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag5.isEnabled = false
            binding.tag5.setTextColor(Color.WHITE)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag6){

            fenceTag = binding.tag6.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag6.isEnabled = false
            binding.tag6.setTextColor(Color.WHITE)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag7){

            fenceTag = binding.tag7.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag7.isEnabled = false
            binding.tag7.setTextColor(Color.WHITE)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag8){
            fenceTag = binding.tag8.text.toString()

            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag8.isEnabled = false
            binding.tag8.setTextColor(Color.WHITE)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag9){

            fenceTag = binding.tag9.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag9.isEnabled = false
            binding.tag9.setTextColor(Color.WHITE)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag10){

            fenceTag = binding.tag10.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag10.isEnabled = false
            binding.tag10.setTextColor(Color.WHITE)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag11){

            fenceTag = binding.tag11.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag11.isEnabled = false
            binding.tag11.setTextColor(Color.WHITE)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag12){

            fenceTag = binding.tag12.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag12.isEnabled = false
            binding.tag12.setTextColor(Color.WHITE)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag13.isEnabled = true
            binding.tag13.setTextColor(Color.BLACK)



        }


        if (p0?.id == R.id.tag13){

            fenceTag = binding.tag13.text.toString()
            binding.tag1.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag1.isEnabled = true
            binding.tag1.setTextColor(Color.BLACK)

            binding.tag2.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag2.isEnabled = true
            binding.tag2.setTextColor(Color.BLACK)

            binding.tag3.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag3.isEnabled = true
            binding.tag3.setTextColor(Color.BLACK)

            binding.tag4.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag4.isEnabled = true
            binding.tag4.setTextColor(Color.BLACK)

            binding.tag5.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag5.isEnabled = true
            binding.tag5.setTextColor(Color.BLACK)

            binding.tag6.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag6.isEnabled = true
            binding.tag6.setTextColor(Color.BLACK)

            binding.tag7.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag7.isEnabled = true
            binding.tag7.setTextColor(Color.BLACK)

            binding.tag8.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag8.isEnabled = true
            binding.tag8.setTextColor(Color.BLACK)

            binding.tag9.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag9.isEnabled = true
            binding.tag9.setTextColor(Color.BLACK)

            binding.tag10.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag10.isEnabled = true
            binding.tag10.setTextColor(Color.BLACK)

            binding.tag11.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag11.isEnabled = true
            binding.tag11.setTextColor(Color.BLACK)

            binding.tag12.background = ContextCompat.getDrawable(applicationContext, R.drawable.light_gray_rounded_bg)
            binding.tag12.isEnabled = true
            binding.tag12.setTextColor(Color.BLACK)

            binding.tag13.background = ContextCompat.getDrawable(applicationContext, R.drawable.blue_rounded_bg)
            binding.tag13.isEnabled = false
            binding.tag13.setTextColor(Color.WHITE)



        }





    }

}