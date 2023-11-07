package com.ibnzahoor98.geomute.helper

import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class Ads {

    companion object{

        fun requestMainActivityAd(view: AdView, context: Context)
        {

            MobileAds.initialize(context) {}
            val adRequest = AdRequest.Builder().build()
            view.loadAd(adRequest)
        }
    }
}