package com.ibnzahoor98.geomute
import android.content.Context
import android.content.Context.MODE_PRIVATE

import android.content.SharedPreferences

class SharedPrefs {

    companion object{

        var  BASIC_SHARED_PREF = "getBasicInfoSharedPref";
        var SHARED_PREF_FENCE_LIMIT = "SHARED_PREF_FENCE_LIMIT"
        var SHARED_PREF_FENCE_COUNT = "SHARED_PREF_FENCE_COUNT"
        var SHARED_PREF_IS_AD_REMOVED= "SHARED_PREF_IS_AD_REMOVED"


        fun getBasicInfoSharedPref(context: Context): SharedPreferences{
            return context.getSharedPreferences(BASIC_SHARED_PREF, MODE_PRIVATE)
        }


        fun  getFenceLimit(context:Context):Int{
            return getBasicInfoSharedPref(context).getInt(SHARED_PREF_FENCE_LIMIT, 2)
        }
        fun  getFenceCount(context:Context):Int{
            return getBasicInfoSharedPref(context).getInt(SHARED_PREF_FENCE_COUNT, 0)
        }
        fun  isAdRemoved(context:Context):Boolean{
            return getBasicInfoSharedPref(context).getBoolean(SHARED_PREF_IS_AD_REMOVED, false)
        }
    }



}