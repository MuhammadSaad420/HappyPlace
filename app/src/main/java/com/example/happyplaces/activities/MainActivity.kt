package com.example.happyplaces.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding?.root)
        binding?.fabAddHappyPlace?.setOnClickListener {
            var intent = Intent(this, AddHappyPlacesActivity::class.java)
            startActivity(intent);
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0) {
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)

        } else {
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }
    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {

        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.rvHappyPlacesList?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter( happyPlacesList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter
    }
}