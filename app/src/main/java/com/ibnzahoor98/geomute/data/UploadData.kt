package com.ibnzahoor98.geomute.data

import com.google.firebase.firestore.GeoPoint
import java.sql.Timestamp

data class UploadData(
    val name:String,
    val radius:Double,
    val tag:String,
    var timestamp: Long,
    var coordinates: GeoPoint,
    var fenceId: String

)
//var muteMode: String