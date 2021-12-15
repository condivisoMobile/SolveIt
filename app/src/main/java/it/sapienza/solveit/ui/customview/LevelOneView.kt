package it.sapienza.solveit.ui.customview

import android.R.attr
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
import android.graphics.Bitmap


class LevelOneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var mRotation: Sensor? = null
    private lateinit var image: Bitmap
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here.
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }
    private var counter = 0
    private var rotating = false

    init {
        isClickable = false

        // SENSOR
        sensorManager = (context as? Activity)?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // Need to register the listener when the fragment is on foregroung, so init of the view
        mRotation.also { grav ->
            sensorManager.registerListener(this,grav,SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawScene(canvas)
    }

    fun drawScene(canvas: Canvas) {
        var oldImage = BitmapFactory.decodeStream((context as? Activity)?.assets?.open("rock_1920.png"))
        image = Bitmap.createScaledBitmap(oldImage, (0.5f * width).toInt(), (0.2 * height).toInt(),false)
        if (oldImage != image) {
            oldImage.recycle();
        }

        var xCenter = (width-image.width) / 2f
        var yCenter = (height-image.height) / 2f

        // create a matrix for the manipulation
        val matrix = Matrix()
        matrix.postRotate(-counter.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, false)

        // This is the rock that must move TODO: sistemare la rotazione!! problema con baricentro della roccia?
        canvas.drawBitmap(rotatedBitmap,-counter*0.5f + (width-image.width)/2f,-counter*0.2f + (height-image.height)/2f,null)

        // Creating other rocks in different positions
        canvas.drawBitmap(image,(width-image.width*2f), (height-image.height*2f),null)
        canvas.drawBitmap(image,(width-image.width)/0.9f,(height-image.height*3).toFloat(),null)
        canvas.drawBitmap(image,(width-image.width)/3f,(height-image.height)/4f,null)

    }

    // Overriding sensors function, extracting info from them
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //Log.d("Sens accuracy", "Accuracy"+p1.toString())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        /*  values[0]: x*sin(θ/2)   values[1]: y*sin(θ/2)
            values[2]: z*sin(θ/2)   values[3]: cos(θ/2)
            values[4]: estimated heading Accuracy (in radians) (-1 if unavailable)
         */
        val zValue = event?.values?.get(2)
        if (zValue != null) {
            //if (Math.abs(zValue) < 0.2 && !rotating) {
            if (Math.abs(zValue) < 0.2 ) {
                rotating = true     // TODO: need to send this info to the fragment that activate the dialog button
                counter+=2

                // TODO: Need to unregister the listener when the animation is executed, JUST uncomment
                //sensorManager.unregisterListener(this)

                // Redraw scene
                invalidate()
            }
        }
    }
}