package com.ibnzahoor98.geomute

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.databinding.ActivityMainBinding
import com.ibnzahoor98.geomute.databinding.NavHeaderMainBinding
import com.ibnzahoor98.geomute.helper.Ads
import com.ibnzahoor98.geomute.helper.Spotlight
import com.wooplr.spotlight.SpotlightConfig
import com.wooplr.spotlight.SpotlightView
import com.wooplr.spotlight.utils.SpotlightSequence


class Main : AppCompatActivity(), View.OnClickListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: NavHeaderMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    val db = Firebase.firestore
    var auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.hamburger.setOnClickListener(this)
        setSupportActionBar(binding.toolbar)
        getSupportActionBar()?.hide()
        drawerLayout = binding.drawerLayout
        createFirestoreDoc()
        addingRestriction()


        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboard, R.id.auth, R.id.store, R.id.contact, R.id.about, R.id.credits
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setupClickListener()
        headerBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))


         if (!SharedPrefs.isAdRemoved(this))
        {
            binding.adViewMainActivity.visibility = View.VISIBLE
            headerBinding.adViewNavHeader.visibility = View.VISIBLE
            Ads.requestMainActivityAd(binding.adViewMainActivity, this)
            adMainActivityListener()
            Ads.requestMainActivityAd(headerBinding.adViewNavHeader, this)
            adNavHeaderListener()
        }


        populatingNavHeader()

//        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

//
//        val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//        object : CountDownTimer(5000, Constants.INTERVAL){
//            override fun onTick(p0: Long) {
//            }
//
//            override fun onFinish() {
//
//                val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//            }
//
//        }.start();

        spotlight()
    }

    fun spotlight(){


        SpotlightView.Builder(this@Main)
            .introAnimationDuration(400)
            .enableRevealAnimation(false)
            .performClick(true)
            .fadeinTextDuration(400)
            .headingTvColor(Color.parseColor("#ffffff"))
            .headingTvSize(32)
            .headingTvText("Geo Fence Info")
            .subHeadingTvColor(Color.parseColor("#ffffff"))
            .subHeadingTvSize(16)
            .subHeadingTvText("Indicates total number of silent geo fences you can create and how many you have created.")
            .maskColor(Color.parseColor("#dc000000"))
            .target(binding.hideShow)
            .lineAnimDuration(400)
            .lineAndArcColor(Color.parseColor("#ffffff"))
            .dismissOnTouch(true)
            .dismissOnBackPress(true)
            .enableDismissAfterShown(true)
            .usageId("529909") //UNIQUE ID
            .setTypeface(ResourcesCompat.getFont(applicationContext, R.font.u_semi_bold))

            .setListener {

                SpotlightView.Builder(this@Main)
                    .introAnimationDuration(400)
                    .enableRevealAnimation(false)
                    .performClick(true)
                    .fadeinTextDuration(400)
                    .headingTvColor(Color.parseColor("#ffffff"))
                    .headingTvSize(32)
                    .headingTvText("Navigation Menu")
                    .subHeadingTvColor(Color.parseColor("#ffffff"))
                    .subHeadingTvSize(16)
                    .subHeadingTvText("Click to access navigation menu section.")
                    .maskColor(Color.parseColor("#dc000000"))
                    .target(binding.hamburger)
                    .lineAnimDuration(400)
                    .lineAndArcColor(Color.parseColor("#ffffff"))
                    .dismissOnTouch(true)
                    .dismissOnBackPress(true)
                    .enableDismissAfterShown(true)
                    .usageId("599049") //UNIQUE ID
                    .setTypeface(ResourcesCompat.getFont(applicationContext, R.font.u_semi_bold))

                    .setListener {

                    }
                    .show()

            }
            .show()


//        SpotlightSequence.getInstance(this@Main,Spotlight.init(this))
//            .addSpotlight(binding.hideShow, "Geo Fence Info", "Indicates total number of silent geo fences you can create and how many you have created.", "9292")
//            .addSpotlight(binding.hamburger, "Navigation Menu", "Click to access navigation menu section.", "9192")
//
//            .startSequence();


    }


        fun startReviewFlow() {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
        }
    fun populatingNavHeader(){

            if (auth.currentUser != null)
            {
                headerBinding.name.text = auth.currentUser?.email.toString()
                headerBinding.id.text = "ID: " + auth.currentUser?.uid.toString()
                headerBinding.userType.text = "Registered"

            }else{

                headerBinding.name.text = "Guest user"
                headerBinding.id.text = "ID: " + Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                headerBinding.userType.text = "Guest"
            }



        }


    fun adMainActivityListener(){
        binding.adViewMainActivity.adListener = object: AdListener() {
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
    fun adNavHeaderListener(){
        headerBinding.adViewNavHeader.adListener = object: AdListener() {
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



    private fun setupClickListener() {
        // Set item click listener to perform action on menu item click.
        binding.navView.setNavigationItemSelectedListener { menuItem -> // Toggle the checked state of menuItem.
            menuItem.isChecked = !menuItem.isChecked
            when (menuItem.itemId) {
                R.id.nav_dashbaord -> {
                    navController.navigate(R.id.dashboard)

                    addingRestriction()
                }
                R.id.nav_login_register -> {
                    navController.navigate(R.id.auth)
                    binding.fencesLimitValue.text = "Auth"
                }
                R.id.nav_store -> {
                    navController.navigate(R.id.store)
                    binding.fencesLimitValue.text = "Store"
                }
                R.id.nav_review ->{
                    startReviewFlow()
                }
                R.id.nav_contact ->{
                    navController.navigate(R.id.contact)
                    binding.fencesLimitValue.text = "Contact"
                }
                R.id.nav_about ->{
                    navController.navigate(R.id.about)
                    binding.fencesLimitValue.text = "About"
                }
                R.id.nav_credits ->{
                    navController.navigate(R.id.credits)
                    binding.fencesLimitValue.text = "Credits"
                }
            }

            closeDrawer()
            true
        }
    }
    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }




    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    fun createFirestoreDoc(){

        var collection = ""
        var document = ""

        var deviceId = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        collection = "guests"
        document = deviceId

        db.collection(collection).document(document)
            .get()
            .addOnSuccessListener { documentReference ->
                if (!documentReference.exists())
                {
                    var data = hashMapOf<String, Any>(
                        "uid" to document,
                        "isAdRemoved" to false,
                        "fenceLimit" to 2
                    )
                    db.collection(collection)
                        .document(document)
                        .set(data)

                    SharedPrefs.getBasicInfoSharedPref(this).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_LIMIT, 2).apply()
                    SharedPrefs.getBasicInfoSharedPref(this).edit().putBoolean(SharedPrefs.SHARED_PREF_IS_AD_REMOVED, false).apply()

                }
            }
            .addOnFailureListener { e ->

            }


    }

    override fun onResume() {
        super.onResume()
        addingRestriction()
        hideLoginRegister()
    }

    fun addingRestriction(){

        val localCount = SharedPrefs.getFenceCount(this)

        if (auth.currentUser == null)
        {

            binding.fencesLimitValue.text = "$localCount / "+SharedPrefs.getFenceLimit(this)

        }else {


            binding.fencesLimitValue.text = "$localCount / "+SharedPrefs.getFenceLimit(this)

        }



    }


    fun hideLoginRegister(){
        var nav_Menu = binding.navView.menu
        if (auth.currentUser != null)
        {

            nav_Menu.findItem(R.id.nav_login_register).setVisible(false)
        }else{
            nav_Menu.findItem(R.id.nav_login_register).setVisible(true)
        }
    }



    override fun onClick(p0: View?) {

        if (p0?.id == R.id.hamburger)
        {

            drawerLayout.open()

        }


    }


}