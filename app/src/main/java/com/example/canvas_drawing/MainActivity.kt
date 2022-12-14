package com.example.canvas_drawing

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linerLayoutPainrColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linerLayoutPainrColors[2] as ImageButton

        mImageButtonCurrentPaint!!.setImageDrawable(

            ContextCompat.getDrawable(
                this, R.drawable.pallet_selected
            )

        )


        drawingView = findViewById(R.id.drawing_view)

        drawingView?.setBrushSize(20.toFloat())


        val ib_brush: ImageButton = findViewById(R.id.ib_brush)



        ib_brush.setOnClickListener {


            showBrushSizeDialog()
        }
    }


    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)



        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)


        smallBtn.setOnClickListener {

            Log.i("Small button clicked", " Failded")

            drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)


        mediumBtn.setOnClickListener {

            Log.i("Small button clicked", " Failded")

            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)


        largeBtn.setOnClickListener {

            Log.i("Small button clicked", " Failded")

            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()

    }
}