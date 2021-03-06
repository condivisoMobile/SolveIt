package it.sapienza.solveit.ui.customview

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import it.sapienza.solveit.R
import it.sapienza.solveit.ui.levels.single.SingleLevelTwoFragment
import java.lang.ClassCastException

class SingleLevelTwoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {
    private var isMorning: Boolean = true
    private var isNear: Boolean = false
    private var isDark: Boolean = false
    private var sensorManager: SensorManager
    private var mLight: Sensor? = null
    private var mProximity: Sensor? = null

    private lateinit var fragmentManager: FragmentManager
    private var parentFrag: SingleLevelTwoFragment?


    private lateinit var image: Bitmap
    private var counter = 0

    init {
        isClickable = false

        // Retrieve parent fragment
        try {
            fragmentManager = (context as FragmentActivity).supportFragmentManager
        } catch (e: ClassCastException) {
            Log.e("Error fragment manager", "Can't get fragment manager")
        }
        parentFrag  = fragmentManager.findFragmentById(R.id.fragmentContainerView) as SingleLevelTwoFragment?

        // SENSOR
        sensorManager =
            (context as? Activity)?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        mProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        mLight.also { grav ->
            sensorManager.registerListener(this, grav, SensorManager.SENSOR_DELAY_UI)}
        mProximity.also { grav ->
            sensorManager.registerListener(this, grav, SensorManager.SENSOR_DELAY_UI)}
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isMorning) {
            val oldImage = BitmapFactory.decodeStream((context as? Activity)?.assets?.open("sun_removebg.png"))
            image = Bitmap.createScaledBitmap(oldImage, (0.7f*width).toInt(),(0.5*height).toInt(),false)
            if (oldImage!= image){
                oldImage.recycle()
            }
        } else {
            val oldImage = BitmapFactory.decodeStream((context as? Activity)?.assets?.open("moon_removebg.png"))
            image = Bitmap.createScaledBitmap(oldImage, (0.7f*width).toInt(),(0.5*height).toInt(),false)
            if (oldImage!= image){
                oldImage.recycle()
            }
            sensorManager.unregisterListener(this)
            // Activating the button on the fragment for the win dialog
            parentFrag!!.view?.let { parentFrag!!.activateButton(it) }
        }

        canvas.drawBitmap(image,counter*10+0f,0f,null)

    }

    override fun onSensorChanged(event: SensorEvent) {
        // Just look when light is on or off
        if(event.sensor.type == Sensor.TYPE_LIGHT) {
            if (event.values[0] <= 3.0f) {
                // Light is off, sun sleeps
                isDark = true

            }
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] <= 4.0f) {
                isNear = true
            }
        }

        isMorning = !(isDark && isNear)
        if(!isMorning) {
            invalidate()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // nothing
    }
}