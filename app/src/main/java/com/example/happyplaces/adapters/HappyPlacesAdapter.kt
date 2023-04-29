package com.example.happyplaces.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlacesAdapter(private val items:List<HappyPlaceModel>,
                 private val onItemClicked: (position: Int, happyPlaceModel: HappyPlaceModel) -> Unit
): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivImage = binding.ivPlaceImage;
        val tvTitle = binding.tvTitle;
        val tvDescription = binding.tvDescription;
        val llm = binding.llm

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context;
        val currentItem = items[position];
        holder.tvTitle.text = currentItem.title;
        holder.tvDescription.text = currentItem.description;
        holder.ivImage.setImageURI(Uri.parse(currentItem.image))
        holder.llm.setOnClickListener {
            onItemClicked.invoke(position, currentItem)
        }
    }
}
