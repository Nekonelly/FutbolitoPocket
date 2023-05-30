package com.example.futbolitopocket

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var pelota: MiViewDibujado? = null
    private lateinit var sensorManager: SensorManager

    private var goalsCount = 0

    private val magneticLinesPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 5f
        //pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pelota = MiViewDibujado(this)
        setContentView(pelota)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xAcc = event.values[0]
            val yAcc = event.values[1]
            pelota?.updatePosition(xAcc, yAcc)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    inner class MiViewDibujado(context: Context) : View(context) {
        private val pincel = Paint().apply {
            color = Color.RED
        }

        private var xPos = 0f
        private var xVelocity = 0f
        private var yPos = 0f
        private var yVelocity = 0f

        private var screenWidth = 0
        private var screenHeight = 0

        private val ballRadius = 50.0f

        private val startXTop = 0f
        private val stopXTop: Float
        private val startYTop: Float
        private val stopYTop: Float

        private val startXBottom = 0f
        private val stopXBottom: Float
        private val startYBottom: Float
        private val stopYBottom: Float

        init {
            val displayMetrics = resources.displayMetrics
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels

            stopXTop = screenWidth.toFloat()
            startYTop = (screenHeight / 9.8f)
            stopYTop = startYTop

            stopXBottom = screenWidth.toFloat()
            startYBottom = (screenHeight * 3 / 3.5).toFloat()
            stopYBottom = startYBottom
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            canvas?.drawColor(Color.rgb(0, 100, 0))

            canvas?.drawLine(startXTop, startYTop, stopXTop, stopYTop, magneticLinesPaint)
            canvas?.drawLine(startXBottom, startYBottom, stopXBottom, stopYBottom, magneticLinesPaint)

            canvas?.drawCircle(xPos, yPos, ballRadius, pincel)
            //Dibujar contador de goles
            val textPaint = Paint().apply {
                textSize = 50f
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
            }
            val goalsText = "Goals: $goalsCount"
            val textX = screenWidth / 8f
            val textY = screenHeight / 2f
            canvas?.rotate(90f, textX, textY)
            canvas?.drawText(goalsText, textX, textY, textPaint)

            invalidate()
        }

        fun updatePosition(xOrientation: Float, yOrientation: Float) {
            xVelocity = -xOrientation * 2f
            yVelocity = yOrientation * 2f
            updateX()
            updateY()
        }

        private fun updateX() {
            xPos += xVelocity

            if (xPos < ballRadius) {
                xPos = ballRadius
                xVelocity = -xVelocity
            } else if (xPos > screenWidth - ballRadius) {
                xPos = screenWidth - ballRadius
                xVelocity = -xVelocity
            }
        }

        private fun updateY() {
            yPos += yVelocity

            if (yPos < ballRadius) {
                yPos = ballRadius
                yVelocity = -yVelocity
            } else if (yPos > startYBottom - ballRadius) {
                yPos = startYBottom - ballRadius
                yVelocity = -yVelocity
            }

            // Verificar si se ha metido un gol
            if ((yPos >= startYTop && yPos <= startYTop + ballRadius) ||
                (yPos <= startYBottom && yPos >= startYBottom - ballRadius)
            ) {
                // Gol: la pelota ha cruzado una de las líneas magnéticas (porterías)
                goalsCount++
                xPos = screenWidth / 2f
                yPos = screenHeight / 2f
            }
        }
    }
}
