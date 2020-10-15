package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.view.View
import android.widget.TextView


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager : SensorManager
    private var mAccelerometer : Sensor ?= null
    private var mProximity : Sensor ?= null
    private var mAmb_Temp : Sensor ?= null
    private var mLight : Sensor ?= null
    private var mPressure : Sensor ?= null
    private var mGyroscope : Sensor ?= null
    private var resume = false;

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && resume) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                findViewById<TextView>(R.id.Acc_value).text = "ACCELEROMETER: "+event.values[0].toString()
            }
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                findViewById<TextView>(R.id.prox_value).text = "PROXIMITY: "+event.values[0].toString()
            }
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                findViewById<TextView>(R.id.am_temp_value).text = "AMBIENT_TEMPERATURE: "+event.values[0].toString()
            }
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                findViewById<TextView>(R.id.Light).text = "Light: "+event.values[0].toString()
            }
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                findViewById<TextView>(R.id.Pressure).text = "Pressure: "+event.values[0].toString()
            }
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                findViewById<TextView>(R.id.GYROSCOPE).text = "GYROSCOPE: "+event.values[0].toString()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        mAmb_Temp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mAmb_Temp, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun resumeReading(view: View) {
        this.resume = true
    }

    fun pauseReading(view: View) {
        this.resume = false
    }
}



