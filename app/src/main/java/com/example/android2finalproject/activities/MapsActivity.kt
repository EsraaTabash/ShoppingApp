package com.example.android2finalproject.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.android2finalproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.type.LatLng

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    //we will read lat/lng from intent extras (passed from fragment)
    private var lat: Double? = null
    private var lng: Double? = null
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //get extras (product location)
        lat   = intent.getDoubleExtra("lat", Double.NaN)
        lng   = intent.getDoubleExtra("lng", Double.NaN)
        title = intent.getStringExtra("title") ?: "Location"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //when map ready we will show marker and move camera
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //basic ui (same spirit as your sample)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true

        val la = lat
        val lo = lng
        if (la == null || lo == null || la.isNaN() || lo.isNaN()) {
            Toast.makeText(this, "no location to show", Toast.LENGTH_SHORT).show()
            return
        }

        val point = LatLng(la, lo)
        val marker = MarkerOptions()
            .position(point)
            .title(title ?: "Picked location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .snippet("Details: ${title ?: ""}")

        mMap.addMarker(marker)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14f))

        // مثال بسيط لشكل خط/دائرة (اختياري):
        // mMap.addCircle(CircleOptions().center(point).radius(100.0).strokeColor(Color.BLUE).fillColor(Color.argb(60, 33, 150, 243)))
    }
}
