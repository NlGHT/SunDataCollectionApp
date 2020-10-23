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
import android.view.View
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



class MainActivity2 : AppCompatActivity(), SensorEventListener {
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String
    private var timesun: String? = null
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

    private var i = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Reset sun button, take you to main activity again
        val resetSun = findViewById<Button>(R.id.btnReSun)
        resetSun.setOnClickListener{
            val intentReset = Intent(this, MainActivity::class.java)
            startActivity(intentReset)
        }

        //Take next pictrue for the same sun
        findViewById<Button>(R.id.btnPic).setOnClickListener { view ->
            dispatchTakePictureIntent() }


        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Using TYPE_ORIENTATION is deprecated and replaced with TYPE_ROTATION_VECTOR
        sRotationVectors = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sAmbientLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sAmbTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        // Location
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // This will be called when the location changes as by minTime and minDistance set
                // on creation (see startLocation()->else)
                locationChanged(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            }
        }


    private fun startLocation() {
        Log.v("startLocation()", "Called")

        // First check if the app has the location permissions allowed
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            // If permission to use location has not been granted, then request them
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )

            // Callback is set for responding to the permissions request popup
            ActivityCompat.OnRequestPermissionsResultCallback {
                    requestCode,
                    permissions,
                    grantResults ->
                // This will run after the permission request has been allowed/denied
                startLocation()
            }
        } else {
            // If the permission has been granted then check is the GPS has been turned on
            if (isGPSOn()) {
                // Start receiving location updates using the locationListner defined in onCreate
                // for all the callbacks
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    5f,
                    locationListener
                )

                // Set the global var that the GPS is running
                gpsRunning = true;
                Log.v("startLocation()", "Location updates started...")
            } else {
                // If the GPS is not turned on the user will need to turn it on and reload this function
                // (Maybe there is a way to do this programmatically idk but it works fine)
                Toast.makeText(
                    this,
                    "Turn on GPS and reload/restart/refocus app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Check if permission for location granted
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isGPSOn() : Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun locationChanged(location: Location) {
        // This will be called from the locationListener's instantiation in onCreate()
        this.latitude = location.latitude
        this.longitude = location.longitude
        Log.v("locationChanged", "Longitude recorded: " + this.longitude.toString())
        Log.v("locationChanged", "Latitude recorded: " + this.latitude.toString())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && resume) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> SensorManager.getRotationMatrixFromVector(
                    rotationMatrix,
                    event.values
                )
                Sensor.TYPE_LIGHT -> this.fAmbientLight = event.values[0]
                Sensor.TYPE_MAGNETIC_FIELD -> this.mMagneticField = event.values
                Sensor.TYPE_GRAVITY -> this.mGravity = event.values
                Sensor.TYPE_GYROSCOPE -> this.vGyroscope = event.values
                Sensor.TYPE_ACCELEROMETER -> this.vAccellerometer = event.values
                Sensor.TYPE_AMBIENT_TEMPERATURE -> this.fAmbTemp = event.values[0]
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, sRotationVectors, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sAmbientLight, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sMagneticField, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sGravity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, sAmbTemp, SensorManager.SENSOR_DELAY_NORMAL)

        // Start the location updates when entering the app again
        startLocation()
    }

    override fun onPause() {
        super.onPause()
        if (!gpsRunning)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Stop the location recording outside the app
                locationManager.removeUpdates(locationListener)
                gpsRunning = true;
            }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }

                galleryAddPic()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val sunFile: String? = intent.getStringExtra("sunTime")
        timesun = sunFile
        i++
        return  File(storageDir, "PIC_${i}_${timesun}" + ".jpg"
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }
}