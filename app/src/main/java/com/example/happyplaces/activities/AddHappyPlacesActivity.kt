package com.example.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlacesBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlacesActivity : AppCompatActivity(), View.OnClickListener {
    var binding: ActivityAddHappyPlacesBinding? = null
    var cal:Calendar = Calendar.getInstance();
    var galleryResultLauncher: ActivityResultLauncher<Intent?>? = null
    var cameraResultLauncher: ActivityResultLauncher<Intent?>? = null
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlacesBinding.inflate(layoutInflater);
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateTextView()
        }
        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if(data != null) {
                    val contentUri = data.data;
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                        saveToInternalStorage(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }


            }
        }

        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if(data != null) {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    try {
                        binding?.ivPlaceImage?.setImageBitmap(thumbnail)
                        saveToInternalStorage(thumbnail)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }


            }
        }

    }

    private fun updateTextView() {
        val myFormat = "MM/dd/yy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding?.etDate?.setText(dateFormat.format(cal.getTime()))
    }

    override fun onClick(v: View?) {
        Log.e("pressed", "yes")
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlacesActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                var myDialog = AlertDialog.Builder(this)
                myDialog.setTitle("Pick Option")
                val items = arrayOf("Gallery", "Camera")
                myDialog.setItems(items) {_, which ->
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> choosePhotoFromCamera()
                    }

                }
                myDialog.show()
            }
        }
    }

    private fun choosePhotoFromCamera() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()) {
                        val photoPickerIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraResultLauncher?.launch(photoPickerIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showPermissionDeniedDialog()
                }
            }).check()
    }
    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        val directory: File = cw.getDir("HappyPlaces", Context.MODE_PRIVATE)
        val mypath = File(directory, "${UUID.randomUUID()}.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()) {
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        galleryResultLauncher?.launch(photoPickerIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showPermissionDeniedDialog()
                }
            }).check()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this).setTitle("Permission Alert")
            .setPositiveButton("Go to settings") {_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }.show()
    }


}