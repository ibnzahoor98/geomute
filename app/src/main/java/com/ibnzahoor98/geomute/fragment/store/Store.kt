package com.ibnzahoor98.geomute.fragment.store

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.ImmutableList
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.SharedPrefs
import com.ibnzahoor98.geomute.databinding.FragmentStoreBinding
import com.ibnzahoor98.geomute.helper.TransferGuestToUser


class Store : Fragment(), View.OnClickListener{

    private lateinit var binding: FragmentStoreBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var billingClient: BillingClient

    private lateinit var adRemoveProductDetail:ProductDetails;
    private lateinit var fenceLimitProductDetail:ProductDetails
    private var WHICH_DIALOG_CLICKED = 0
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentStoreBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStoreBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        binding.removeAd.setOnClickListener(this)
        binding.fenceLimit.setOnClickListener(this)
        initialPaymentChecks()
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        initBilling()
        initialPaymentChecks()

        return binding.root
    }

    fun initialPaymentChecks(){
        val IS_AD_REMOVED = SharedPrefs.getBasicInfoSharedPref(requireContext()).getBoolean(SharedPrefs.SHARED_PREF_IS_AD_REMOVED, false)

        if (IS_AD_REMOVED){

            binding.removeAdPurchased.visibility = View.VISIBLE;
            binding.removeAdsAmount.visibility = View.GONE
            binding.removeAd.isEnabled = false
           // binding.removeAd.background = ContextCompat.getDrawable(requireContext(), R.drawable.white_rounded_bg)

        }
        val FENCE_LIMIT = SharedPrefs.getBasicInfoSharedPref(requireContext()).getInt(SharedPrefs.SHARED_PREF_FENCE_LIMIT, 0)
         if (FENCE_LIMIT > 29){
            binding.fenceLimitPurchased.visibility = View.VISIBLE;
            binding.fenceLimitAmount.visibility = View.GONE
            binding.fenceLimit.isEnabled = false
           // binding.fenceLimit.background = ContextCompat.getDrawable(requireContext(), R.drawable.white_rounded_bg)

        }

    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.i("dev", "successful purchase...")

                    val purchaseToken = purchase.purchaseToken
                    Log.i("dev", "successful purchase..." + purchase.orderId.toString())
                    Log.i("dev", "successful purchase..." + purchase.signature.toString())
                    if (WHICH_DIALOG_CLICKED == 1)
                    {
                            updateFirestoreAdRemove()
                        binding.removeAdPurchased.visibility = View.VISIBLE;
                        binding.removeAdsAmount.visibility = View.GONE
                        binding.removeAd.isEnabled = false
                       // binding.removeAd.background = ContextCompat.getDrawable(requireContext(), R.drawable.white_rounded_bg)

                    }else if (WHICH_DIALOG_CLICKED == 2)
                    {
                        updateFirestoreFenceLimit()
                        binding.fenceLimitPurchased.visibility = View.VISIBLE;
                        binding.fenceLimitAmount.visibility = View.GONE
                        binding.fenceLimit.isEnabled = false
                       // binding.fenceLimit.background = ContextCompat.getDrawable(requireContext(), R.drawable.white_rounded_bg)
                    }


                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {


               // Toast.makeText(requireContext(), "Purchase canceled", Toast.LENGTH_SHORT).show()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

               // Toast.makeText(requireContext(), "Already bought", Toast.LENGTH_SHORT).show()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {

               // Toast.makeText(requireContext(), "Erro: FEATURE_NOT_SUPPORTED", Toast.LENGTH_SHORT).show()
            } else {

              //  Toast.makeText(requireContext(), "Erro: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
             }

        }
    fun initBilling(){
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAdRemoveProductDetail()
                    queryFenceLimitProductDetail()


                } else {
                    // Connection failed
                    Log.e("VVV","BConnection failed: " + billingResult.debugMessage)
                }


            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                Log.e("VVV","onBillingServiceDisconnected")
            }
        })

    }

    fun queryAdRemoveProductDetail(){
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("ad_remove")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productDetailsList ->

            for (pd in productDetailsList)
            {
                adRemoveProductDetail = pd
            }

        }
    }
    fun queryFenceLimitProductDetail(){
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("fence_limit")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productDetailsList ->
            for (pd in productDetailsList)
            {
                fenceLimitProductDetail = pd

            }

        }
    }



    fun LaunchPurchaseFlow(productDetails: ProductDetails?) {
        val productList = ArrayList<ProductDetailsParams>()
        productList.add(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails!!)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productList)
            .build()
        billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
    }


    override fun onClick(p0: View?) {
        if (p0?.id == R.id.removeAd)
        {

            LaunchPurchaseFlow(adRemoveProductDetail)
            WHICH_DIALOG_CLICKED = 1

        }
        if (p0?.id == R.id.fenceLimit)
        {
            LaunchPurchaseFlow(fenceLimitProductDetail)
            WHICH_DIALOG_CLICKED = 2
        }

    }
    fun updateFirestoreAdRemove(){
        var collection = ""
        var document = ""
        if (auth.currentUser == null)
        {
            var deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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

                    val docData = hashMapOf(
                        "isAdRemoved" to true,
                    )
                    SharedPrefs.getBasicInfoSharedPref(requireContext()).edit().putBoolean(SharedPrefs.SHARED_PREF_IS_AD_REMOVED, true).apply()
                    TransferGuestToUser.db.collection(collection)
                        .document(document)
                        .set(docData , SetOptions.merge())

                }


            }
            .addOnFailureListener { e ->

            }



    }

    fun updateFirestoreFenceLimit(){
        var collection = ""
        var document = ""
        if (auth.currentUser == null)
        {
            var deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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

                    val docData = hashMapOf(
                        "fenceLimit" to 30,
                    )
                    SharedPrefs.getBasicInfoSharedPref(requireContext()).edit().putInt(SharedPrefs.SHARED_PREF_FENCE_LIMIT, 30).apply()
                    TransferGuestToUser.db.collection(collection)
                        .document(document)
                        .set(docData , SetOptions.merge())

                }


            }
            .addOnFailureListener { e ->

            }



    }

}
