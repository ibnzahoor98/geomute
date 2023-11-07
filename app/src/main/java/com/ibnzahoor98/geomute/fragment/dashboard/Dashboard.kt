package com.ibnzahoor98.geomute.fragment.dashboard

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.Constants
import com.ibnzahoor98.geomute.CreateGeoFence
import com.ibnzahoor98.geomute.LocationPermissionConsent
import com.ibnzahoor98.geomute.Main
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.SharedPrefs
import com.ibnzahoor98.geomute.data.UploadData
import com.ibnzahoor98.geomute.databinding.FragmentDashboardSuperBinding
import com.ibnzahoor98.geomute.fragment.auth.Auth
import com.ibnzahoor98.geomute.fragment.store.Store
import com.wooplr.spotlight.SpotlightView


class Dashboard : Fragment() , View.OnClickListener {


    private lateinit var binding: FragmentDashboardSuperBinding

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var auth: FirebaseAuth
    private val itemsList = ArrayList<DashboardGeofenceData>()
    private lateinit var dashboardGeofenceAdapter: DashboardGeofenceAdapter

    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDashboardSuperBinding.inflate(layoutInflater)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDashboardSuperBinding.inflate(inflater, container, false)

        binding.container.emptyFenceContainer.visibility = View.GONE
        binding.container.transactionHistoryContainer.visibility = View.GONE
        binding.container.fenceLoading.visibility = View.VISIBLE

        auth = Firebase.auth
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomsheetlayout.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.container.idFABHome.setOnClickListener(this)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        spotlight()


        return binding.root
    }
    fun spotlight(){
        object : CountDownTimer(3000, Constants.INTERVAL){
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {

//                SpotlightView.Builder(requireActivity())
//                    .introAnimationDuration(400)
//                    .enableRevealAnimation(false)
//                    .performClick(true)
//                    .fadeinTextDuration(400)
//                    .headingTvColor(Color.parseColor("#ffffff"))
//                    .headingTvSize(32)
//                    .headingTvText("Create Geo Fence ")
//                    .subHeadingTvColor(Color.parseColor("#ffffff"))
//                    .subHeadingTvSize(16)
//                    .subHeadingTvText("Click on the plus icon to create silent geo fence.")
//                    .maskColor(Color.parseColor("#dc000000"))
//                    .target(binding.container.idFABHome)
//                    .lineAnimDuration(400)
//                    .lineAndArcColor(Color.parseColor("#ffffff"))
//                    .dismissOnTouch(true)
//                    .dismissOnBackPress(true)
//                    .enableDismissAfterShown(true)
//                    .usageId("3") //UNIQUE IDCan you get
//                    .setTypeface(ResourcesCompat.getFont(requireContext(), R.font.u_semi_bold))
//                    .show()
            }

        }.start();



    }


    fun populatingTransactingHistory(){
        dashboardGeofenceAdapter = (DashboardGeofenceAdapter(itemsList, object : DashboardGeofenceAdapter.OnItemClickListener {
            override fun onItemClick(item: DashboardGeofenceData?) {

                if (item != null && item.coordinates != null)
                {
                    val intent = Intent(context, FenceDetailView::class.java)
                        .putExtra("name", item.name)
                        .putExtra("radius", item.radius.toString() + " m")
                        .putExtra("tag", item.tag.toString())
                        .putExtra("timestamp", item.timestamp.toString())
                        .putExtra("latitude", item.coordinates.latitude.toString())
                        .putExtra("longitude", item.coordinates.longitude.toString())
                        .putExtra("fenceId", item.fenceId)
                      //  .putExtra("muteMode", item.muteMode.toString())
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent)
                }


            }
        }))


        binding.container.recyclerView.adapter = dashboardGeofenceAdapter

        val layoutManager = LinearLayoutManager(requireContext())
        binding.container.recyclerView.layoutManager = layoutManager
        binding.container.recyclerView.addItemDecoration(
            DividerItemDecoration(context, layoutManager.orientation)
        )
        prepareItems()
    }

    override fun onResume() {
        super.onResume()
        itemsList.clear()

        populatingTransactingHistory()
    }

    private fun prepareItems() {
        var collection = ""
        var document = ""
        if (auth.currentUser == null)
        {
            var deviceId = Settings.Secure.getString(context?.getContentResolver(), Settings.Secure.ANDROID_ID);
            collection = "guests"
            document = deviceId
        }else {

            collection = "users"
            document = auth.currentUser?.uid.toString()
        }


        val docRef = db.collection(collection).document(document)

        docRef.get().addOnSuccessListener {
                documents ->

            if (documents != null && documents.exists()) {

                binding.container.emptyFenceContainer.visibility = View.GONE
                binding.container.transactionHistoryContainer.visibility = View.VISIBLE
                binding.container.fenceLoading.visibility = View.GONE

                if (documents.data?.get("geofences") != null)
                {
                    val fences = documents.data?.get("geofences") as List<HashMap<String, *>>
                    if (fences != null && fences.size > 0)
                    {
                        STORE_CIRCLES.clear()

                         
                        for (f in fences)
                        {


                            val name = f.get("name") as String
                            val radius = f.get("radius") as Double
                            val tag = f.get("tag") as String
                            val timestamp = f.get("timestamp") as Long
                            val coordinates = f.get("coordinates") as GeoPoint
                            val fenceId = f.get("fenceId") as String
                           // var muteMode = f.get("muteMode") as String
//                            if (muteMode == "1") {muteMode = "Do Not Disturb"}
//                            else if (muteMode == "2"){muteMode = "Vibration"}
                         //   itemsList.add(DashboardGeofenceData(name, radius, tag, timestamp, coordinates, fenceId, muteMode))
                            itemsList.add(DashboardGeofenceData(name, radius, tag, timestamp, coordinates, fenceId))
                            dashboardGeofenceAdapter.notifyDataSetChanged()
                         //   STORE_CIRCLES.add(UploadData(name, radius, tag, timestamp, coordinates, fenceId, muteMode))
                            STORE_CIRCLES.add(UploadData(name, radius, tag, timestamp, coordinates, fenceId))


                        }
                    }
                    else{
                        binding.container.emptyFenceContainer.visibility = View.VISIBLE
                        binding.container.transactionHistoryContainer.visibility = View.GONE
                        binding.container.fenceLoading.visibility = View.GONE
                    }
                }else{
                    binding.container.emptyFenceContainer.visibility = View.VISIBLE
                    binding.container.transactionHistoryContainer.visibility = View.GONE
                    binding.container.fenceLoading.visibility = View.GONE
                }


            }else{

                binding.container.emptyFenceContainer.visibility = View.VISIBLE
                binding.container.transactionHistoryContainer.visibility = View.GONE
                binding.container.fenceLoading.visibility = View.GONE
            }



        }


    }




    companion object {

        var STORE_CIRCLES = ArrayList<UploadData>()
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66


    }

    override fun onClick(p0: View?) {
        if (p0?.id == R.id.idFABHome)
        {

            var fenceCount = SharedPrefs.getFenceCount(requireContext())
            var fenceLimit = SharedPrefs.getFenceLimit(requireContext())

             if (fenceCount >= fenceLimit)
            {
                 var f:Fragment = Fragment()
                // show bottom sheet okay
                if (fenceLimit == 2)
                {
                    binding.bottomsheetlayout.reachedMessage.setText("You have reached maximum limit to create silent geo fences. In order to get more, please login with google and increase your limit to 4 fences.")

                    binding.bottomsheetlayout.connect.text = "Login with google"
                   f = Auth()
                }
                if (fenceLimit == 4)
                {
                    binding.bottomsheetlayout.reachedMessage.setText("You have reached maximum limit to create silent geo fences. In order to get more, please consider buy via store.")
                    binding.bottomsheetlayout.connect.text = "Visit store"
                    f = Store()
                }
                if (fenceLimit == 30)
                {
                    binding.bottomsheetlayout.reachedMessage.setText("You have reached maximum limit to create silent geo fences. At the moment you can't create more than 30 fences.")

                    binding.bottomsheetlayout.connect.visibility = View.GONE

                }

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bottomsheetlayout.connect.setOnClickListener({


                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    if (f!=null)
                    {
                        requireActivity()!!
                            .supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, f)
                            .commitNow()
                    }


                })



            }
            else{





                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED)
                {
                    val intent = Intent(context, LocationPermissionConsent::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)


                } else if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(context, LocationPermissionConsent::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)


                }
                else {
                    val intent = Intent(context, CreateGeoFence::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)
                }

            }




        }

    }





}