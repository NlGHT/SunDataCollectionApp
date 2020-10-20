package com.example.sundatacollectionapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String
    private lateinit var mSensorManager : SensorManager
    private var sAccelerometer : Sensor ?= null
    private var vAccellerometer = FloatArray(3)
    private var sRotationVectors : Sensor ?= null
    // Rotation Matrices
    private val rotationMatrix = FloatArray(9)
    private var sAmbTemp : Sensor ?= null
    private var fAmbTemp : Float ?= 0.0f
    private var sAmbientLight : Sensor ?= null
    private var fAmbientLight : Float ?= 0.0f
    private var sGyroscope : Sensor ?= null
    private var vGyroscope  = FloatArray(3)
    private var sMagneticField : Sensor ?= null
    private var mMagneticField = FloatArray(3)
    private var sGravity : Sensor ?= null
    private var mGravity = FloatArray(3)
    private var resume = false;

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            dispatchTakePictureIntent()
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

//        vOrientation = Array<Float>(5)
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

            // Save the data in here
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
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
