package com.example.sundatacollectionapp

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener  {
    private val REQUEST_IMAGE_CAPTURE = 0
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
    private var resume = true;

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val locationPermissionCode = 2
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var gpsRunning = false;



    private var rotation: String? = null
    private var acc_valuex: String? = null
    private var acc_valuey: String? = null
    private var acc_valuez: String? = null

    private var ambtemp_value: String? = null;
    private var light_value: String? = null;
    private var gyroscope_valuex:String? = null
    private var gyroscope_valuey:String? = null
    private var gyroscope_valuez:String? = null

    private var gravity_value:String? = null
    private var magnetic_value:String? = null
    private var pressure_value:String? = null

    private var getSensorValues = false






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        //findViewById<Button>(R.id.btnCamera).setOnClickListener { view ->
        //            dispatchTakePictureIntent()



        findViewById<Button>(R.id.btnSun).setOnClickListener { view ->
            dispatchTakePictureIntent(Activity.RESULT_CANCELED)

        }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Using TYPE_ORIENTATION is deprecated and replaced with TYPE_ROTATION_VECTOR
        sRotationVectors = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sAmbientLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sAmbTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        Log.v("rotation", sRotationVectors.toString())
        Log.v("amb", sAmbientLight.toString())
        Log.v("magnetic", sMagneticField.toString())
        Log.v("gravity", sGravity.toString())
        Log.v("gyro", sGyroscope.toString())
        Log.v("acc", sAccelerometer.toString())
        Log.v("amb light", sAmbTemp.toString())

        // Location
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // This will be called when the location changes as by minTime and minDistance set
                // on creation (see startLocation()->else)
                locationChanged(location)
            }

            override fun onStatusChanged( provider: String, status: Int, extras: Bundle ) { }
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

    // This is run when the permission popup box is answered e.g location
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
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                 var acc_valx = event.values[0].toString();
                acc_valuex = acc_valx
                var acc_valy = event.values[1].toString();
                acc_valuey = acc_valy
                var acc_valz = event.values[2].toString();
                acc_valuez = acc_valz
                Log.v("baby", "oooh")

            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                 var magnetic_val = event.values[0].toString();
                magnetic_value = magnetic_val

            }
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                 var ambtemp_val= event.values[0].toString();
                ambtemp_value = ambtemp_val


            }
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                var light_val = event.values[0].toString();
                light_value = light_val

            }
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                var pressure_val= event.values[0].toString();
                pressure_value = pressure_val

            }
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                var gyroscope_valx = event.values[0].toString();
                gyroscope_valuex = gyroscope_valx
                var gyroscope_valy = event.values[1].toString();
                gyroscope_valuey = gyroscope_valy
                var gyroscope_valz = event.values[2].toString();
                gyroscope_valuez = gyroscope_valz

            }
            if(event.sensor.type == Sensor.TYPE_GRAVITY){
                var gravity_val = event.values[0].toString()
                gravity_value = gravity_val
            }

            if (getSensorValues == true) {
                Log.v("hola", "ola")
                Log.v("ambtag", light_value.toString())
                Log.v("magnetictag", magnetic_value.toString())
                Log.v("gravitytag", gravity_value.toString())
                Log.v(
                    "gyrotag",
                    "x" + gyroscope_valuex.toString() + " y:" + gyroscope_valuey.toString() + " z:" + gyroscope_valuez.toString()
                )
                Log.v(
                    "acctag",
                    "x" + acc_valuex.toString() + " y:" + acc_valuey.toString() + "z " + acc_valuez.toString()
                )
                Log.v("amblighttag", ambtemp_value.toString())

                getSensorValues = false
            }
        }

        //Log.v("rotation)


        /*
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
         */
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

        //Log.v("rotation", sRotationVecto)


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

    private fun dispatchTakePictureIntent(resultCode: Int) {
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
                    getSensorValues = true
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RESULT_OK)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                }
                galleryAddPic()
            }

/*
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            Log.v("hura", "huraa")
            val photoFile: File = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            galleryAddPic()

        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
        */

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if (resultCode == Activity.RESULT_OK){
           getSensorValues = true
        }*/
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            getSensorValues = true
            val intent = Intent(this, MainActivity2::class.java)
            intent.putExtra("sunTime", timesun)
            startActivity(intent)
            // Save the data in here
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        timesun = timeStamp
        val fileName: String? = "SUN_${timeStamp}"
        return  File(storageDir, fileName + ".jpg"
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
