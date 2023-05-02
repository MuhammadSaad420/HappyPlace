package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    var happyPlaceModel: HappyPlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R
            .layout.activity_map)
        if(intent.hasExtra(HappyPlaceDetailActivity.HAPPY_PLACE_EXTRA_DETAL)) {
            happyPlaceModel = intent.getParcelableExtra<HappyPlaceModel>(HappyPlaceDetailActivity.HAPPY_PLACE_EXTRA_DETAL)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(happyPlaceModel!!.latitude,happyPlaceModel!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position))
        val cameraZoomPosition = CameraPosition.builder().zoom(14f).target(position).build()
        var cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraZoomPosition)
        googleMap.animateCamera(cameraUpdate)
    }
}