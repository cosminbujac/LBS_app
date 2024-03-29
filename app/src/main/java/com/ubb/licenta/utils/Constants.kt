package com.ubb.licenta.utils

import com.google.android.gms.maps.model.BitmapDescriptorFactory

object Constants {
    const val PERMISSION_LOCATION_REQUEST_CODE = 1
    const val IMAGE_REQUEST_CODE =2

    const val ACTION_SERVICE_START = "ACTION_SERVICE_START"
    const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"

    const val NOTIFICATION_CHANNEL_ID  = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME  = "tracker_notification"
    const val NOTIFICATION_ID  = 3

    const val PENDING_INTENT_REQUEST_CODE = 99

    const val LOCATION_USUAL_UPDATE_INTERVAL = 4000L
    const val LOCATION_MAX_UPDATE_INTERVAL = 2000L

    const val DATABASE_URL ="https://licenta-a92c7-default-rtdb.europe-west1.firebasedatabase.app"

    val CLOSE_MARKER_COLOR = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
    val PERSONAL_MARKER_COLOR = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
}