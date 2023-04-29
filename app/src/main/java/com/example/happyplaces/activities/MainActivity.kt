package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.util.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null;
    var addPlaceResultLauncher: ActivityResultLauncher<Intent?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding?.root)
        addPlaceResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            }
        }
        binding?.fabAddHappyPlace?.setOnClickListener {
            var intent = Intent(this, AddHappyPlacesActivity::class.java)
            addPlaceResultLauncher?.launch(intent)
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


        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList, ) { _, happyModal ->
            val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
            intent.putExtra(HAPPY_PLACE_EXTRA, happyModal)
            startActivity(intent)
        }
        binding?.rvHappyPlacesList?.adapter = placesAdapter
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(viewHolder.adapterPosition, addPlaceResultLauncher)
            }
        }
        val itemTouchHelper = ItemTouchHelper(editSwipeHandler)
        itemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)
    }
    companion object {
        var HAPPY_PLACE_EXTRA = "happy_place_extra"
    }
}