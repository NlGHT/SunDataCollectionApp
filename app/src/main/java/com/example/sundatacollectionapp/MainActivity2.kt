package com.example.sundatacollectionapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



class MainActivity2 : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String
    private var timesun: String? = null

    //private val intentOK = Intent(this, MainActivity2::class.java)
    private lateinit var mSensorManager: SensorManager
    private var sAccelerometer: Sensor? = null
    private var vAccellerometer = FloatArray(3)
    private var sRotationVectors: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private var sAmbTemp: Sensor? = null
    private var fAmbTemp: Float? = 0.0f
    private var sAmbientLight: Sensor? = null
    private var fAmbientLight: Float? = 0.0f
    private var sGyroscope: Sensor? = null
    private var vGyroscope = FloatArray(3)
    private var sMagneticField: Sensor? = null
    private var mMagneticField = FloatArray(3)
    private var sGravity: Sensor? = null
    private var mGravity = FloatArray(3)
    private var resume = false;

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val locationPermissionCode = 2
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var gpsRunning = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar))
        //val intent = intent
        val sunFile: String?  = intent.getStringExtra("sunTime")
        Log.v("haha", "" + sunFile)

    }
}