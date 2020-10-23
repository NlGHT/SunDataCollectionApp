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

    private var i = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar))
        //val intent = intent

        //findViewById<Button>(R.id.btnCamera).setOnClickListener { view ->
        //            dispatchTakePictureIntent()

        findViewById<Button>(R.id.btnPic).setOnClickListener { view ->
            dispatchTakePictureIntent()
            /*
            val sunFile: String? = intent.getStringExtra("sunTime")
            Log.v("haha", "" + sunFile)
            timesun = sunFile

             */

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
            //Log.v("pls work", "it does")
            //val intent = Intent(this, MainActivity2::class.java)
            //intent.putExtra("sunTime", timesun)
            //startActivity(intent)
            //startActivity(intentOK)
            // Save the data in here
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        i++
        val sunFile: String? = intent.getStringExtra("sunTime")
        Log.v("haha", "" + sunFile)
        timesun = sunFile
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