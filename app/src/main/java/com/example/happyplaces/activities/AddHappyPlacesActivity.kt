package com.example.happyplaces.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlacesBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
import kotlin.collections.ArrayList


class AddHappyPlacesActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geoCoder: Geocoder;
    var binding: ActivityAddHappyPlacesBinding? = null
    var cal:Calendar = Calendar.getInstance();
    var galleryResultLauncher: ActivityResultLauncher<Intent?>? = null
    var cameraResultLauncher: ActivityResultLauncher<Intent?>? = null
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0;
    private var mLongitude: Double = 0.0;
    private var mHappyPlaceDetails: HappyPlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlacesBinding.inflate(layoutInflater);
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geoCoder = Geocoder(this,Locale.getDefault())
        if (intent.hasExtra(MainActivity.HAPPY_PLACE_EXTRA)) {
            mHappyPlaceDetails  = intent.getParcelableExtra<HappyPlaceModel>(MainActivity.HAPPY_PLACE_EXTRA)
        }
        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            binding?.ivPlaceImage?.setImageURI(saveToInternalStorage)

            binding?.btnSave?.text = "UPDATE"
        }
        if(!Places.isInitialized()) {
            Places.initialize(applicationContext,resources.getString(R.string.google_map_key))
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
        binding?.etLocation?.setText("Berlin, German")
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateTextView()
        }
        updateTextView()
        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if(data != null) {
                    val contentUri = data.data;
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                        saveToInternalStorage = saveToInternalStorage(selectedImageBitmap)
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
                        saveToInternalStorage = saveToInternalStorage(thumbnail)!!
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

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
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
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddHappyPlacesActivity)
                } catch (e: Exception) {

                }
            }
            R.id.tv_select_current_location -> {
                if(isLocationEnabled()) {
                    Dexter.withContext(this@AddHappyPlacesActivity)
                        .withPermissions(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(object: MultiplePermissionsListener {
                            @SuppressLint("MissingPermission")
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if(report!!.areAllPermissionsGranted()) {
                                    val locationRequest = LocationRequest()
                                    locationRequest.interval = 2000
                                    locationRequest.fastestInterval = 1000
                                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                    val locationCallBack = object : LocationCallback() {
                                        override fun onLocationResult(locationResult: LocationResult) {
                                           val location = locationResult.lastLocation
                                            mLatitude = location!!.latitude
                                            mLongitude = location!!.longitude
                                            var addreses:ArrayList<Address> =
                                                geoCoder.getFromLocation(mLatitude,mLongitude,1) as ArrayList<Address>
                                            binding?.etLocation?.setText("${addreses[0].locality}, ${addreses[0].countryName}")
                                        }
                                    }
                                    fusedLocationClient.requestLocationUpdates(locationRequest,locationCallBack,
                                        Looper.myLooper())
                                    Toast.makeText(this@AddHappyPlacesActivity,
                                        "All permissions are granted",
                                        Toast.LENGTH_SHORT).
                                    show()
                                }

                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: MutableList<PermissionRequest>?,
                                p1: PermissionToken?
                            ) {
                                showPermissionDeniedDialog()
                            }

                        }).check()
                } else {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
            R.id.btn_save -> {
                when {
                    binding?.etTitle?.text.toString().isEmpty() -> {
                        Toast.makeText(this,"Please enter title",Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.toString().isEmpty() -> {
                        Toast.makeText(this,"Please enter description",Toast.LENGTH_SHORT).show()
                    }
//                    binding?.etLocation?.text.toString().isEmpty() -> {
//                        Toast.makeText(this,"Please enter location",Toast.LENGTH_SHORT).show()
//                    }
                    saveToInternalStorage == null -> {
                        Toast.makeText(this,"Please select image",Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        var happyPlace = HappyPlaceModel(
                            mHappyPlaceDetails?.id ?: 0,
                            binding?.etTitle?.text.toString(),
                            saveToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        var dbHandler = DatabaseHandler(this)
                        if(mHappyPlaceDetails != null) {
                            var updatehappy = dbHandler.updateHappyPlace(happyPlace)
                            if(updatehappy > 0) {
                                Toast.makeText(this,"Place updated successfully",Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            var addHappyPlace = dbHandler.addHappyPlace(happyPlace)
                            if(addHappyPlace > 0) {
                                Toast.makeText(this,"Place stored successfully",Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }

                    }
                }

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
    private fun saveToInternalStorage(bitmapImage: Bitmap): Uri? {
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
        return Uri.parse(mypath.absolutePath)
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