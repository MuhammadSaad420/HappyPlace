package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlacesBinding
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    companion object {
        val HAPPY_PLACE_EXTRA_DETAL = "happyPlaceDetailExtra"
    }
    var binding: ActivityHappyPlaceDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        var happyPlaceModel: HappyPlaceModel? = null;

        if(intent.hasExtra(MainActivity.HAPPY_PLACE_EXTRA)) {
            happyPlaceModel = intent.getParcelableExtra<HappyPlaceModel>(MainActivity.HAPPY_PLACE_EXTRA)
        }
        if(happyPlaceModel != null) {
            setSupportActionBar(binding?.toolbarHappyPlaceDetails)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setTitle(happyPlaceModel.title)
            binding?.toolbarHappyPlaceDetails?.setNavigationOnClickListener {
                onBackPressed()
            }
            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceModel.image))
            binding?.tvDescription?.text = happyPlaceModel.description
            binding?.btnViewOnMap?.setOnClickListener {
                val intent = Intent(this,MapActivity::class.java)
                intent.putExtra(HAPPY_PLACE_EXTRA_DETAL, happyPlaceModel)
                startActivity(intent)
            }
        }
    }
}