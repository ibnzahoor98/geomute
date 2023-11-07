package com.ibnzahoor98.geomute.helper

import android.location.Location

interface UserLocationUpdate {
    fun locationReceived(location: Location)
}