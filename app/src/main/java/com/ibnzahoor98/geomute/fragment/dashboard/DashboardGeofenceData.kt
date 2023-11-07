package com.ibnzahoor98.geomute.fragment.dashboard

import com.google.firebase.firestore.GeoPoint

data class DashboardGeofenceData (
    val name:String,
    val radius:Double,
    val tag:String,
    var timestamp: Long,
    var coordinates: GeoPoint,
    var fenceId: String

    )
//    var muteMode: String