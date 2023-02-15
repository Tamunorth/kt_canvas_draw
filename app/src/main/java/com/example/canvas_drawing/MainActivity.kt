package com.example.canvas_drawing

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null

    private var mImageButtonCurrentPaint: ImageButton? = null


    var customProgressDialog: Dialog? = null


    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                result ->


            if (result.resultCode == RESULT_OK && result.data != null) {

                val imageBackground: ImageView = findViewById(R.id.iv_background);
                imageBackground.setImageURI(result.data?.data);
            }
        }

    private val localStoragePermission: ActivityResultLauncher<Array<String>> =

        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {


                permissions ->


            permissions.entries.forEach {

                val permissionName = it.key
                val permissionsValue = it.value


                if (permissionsValue) {

//                    Toast.makeText(this, "Storage Permission granted", Toast.LENGTH_LONG).show()


                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    openGalleryLauncher.launch(pickIntent);


                } else {

                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {

                        Toast.makeText(this, "Storage Permission Denied!", Toast.LENGTH_LONG).show()

                    }
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linerLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        val ibBrush: ImageButton = findViewById(R.id.ib_brush)
        val undoBtn: ImageButton = findViewById(R.id.undo_btn)
        val redoBtn: ImageButton = findViewById(R.id.redo_btn)
        val saveButton: ImageButton = findViewById(R.id.save_btn)
        val selectImageButton: ImageButton = findViewById(R.id.img_btn)


        mImageButtonCurrentPaint = linerLayoutPaintColors[1] as ImageButton

        mImageButtonCurrentPaint!!.setImageDrawable(

            ContextCompat.getDrawable(
                this, R.drawable.pallet_selected
            )
        )
        drawingView = findViewById(R.id.drawing_view)

        drawingView?.setBrushSize(20.toFloat())


        ibBrush.setOnClickListener {
            showBrushSizeDialog()
        }


        undoBtn.setOnClickListener {

            drawingView?.onClickUndo();
        }
        redoBtn.setOnClickListener {

            drawingView?.onClickRedo();
        }

        saveButton.setOnClickListener {


            if (isReadStorageAllowed()) {


                showProgressDialog();
                lifecycleScope.launch {


                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)



                    saveBitmapFile(getBitmapFromView(flDrawingView));

                }
            }

        }


        selectImageButton.setOnClickListener {
            getStoragePermission()
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

            Log.i("Small button clicked", " Failed")

            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()

    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton;
            val colorTag = imageButton.tag.toString();
            drawingView?.setColor(colorTag);


            imageButton.setImageDrawable(

                ContextCompat.getDrawable(
                    this, R.drawable.pallet_selected
                )
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.pallet_normal
                )
            )
            mImageButtonCurrentPaint = view;
//            Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();

        }
    }

    private fun isReadStorageAllowed(): Boolean {


        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private fun getStoragePermission() {


        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {

            showRationaleDialog(
                "External Storage access is required",
                "Permission has been permanently denied so feature is unavailable"
            )
        } else {
            localStoragePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    ///TODO: Add external storage permission
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.CAMERA
                )
            )
        }


    }


    private fun showRationaleDialog(title: String, message: String) {


        val builder: AlertDialog.Builder = AlertDialog.Builder(this);


        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setNeutralButton("Cancel") { dialog, _ ->


                dialog.dismiss();

            }
            .create().show()

    }


    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)


        val canvas = Canvas(returnedBitmap)

        val bg = view.background;
        if (bg != null) {


            bg.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }


    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {

        var result = "";
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {

                    val bytes = ByteArrayOutputStream();

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)


                    val f =
                        File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidDrawingApp_" + System.currentTimeMillis() / 1000 + ".png")


                    val fo = FileOutputStream(f);


                    fo.write(bytes.toByteArray());
                    fo.close();


                    result = f.absolutePath



                    runOnUiThread {

                        cancelProgressDialog();

                        if (result.isNotEmpty()) {


                            Toast.makeText(
                                this@MainActivity,
                                "File Saved Successfully :$result",
                                Toast.LENGTH_LONG
                            ).show()

                            shareImage(result);


                        } else {

                            Toast.makeText(
                                this@MainActivity,
                                "Something went worng while saving file :$result",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {


                    e.printStackTrace();

                }
            }
        }
        return result;
    }


    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)


        customProgressDialog?.setContentView(R.layout.custom_progress_dialog)


        customProgressDialog?.show();

    }

    private fun cancelProgressDialog() {

        if (customProgressDialog != null) {
            customProgressDialog?.cancel();

            customProgressDialog = null;

        }
    }

    private fun shareImage(result: String) {


        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) {

                path, uri ->
            val shareIntent = Intent();
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }


    }

}
