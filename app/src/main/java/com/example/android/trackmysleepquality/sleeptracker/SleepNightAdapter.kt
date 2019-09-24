package com.example.android.trackmysleepquality.sleeptracker

import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil

import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.android.trackmysleepquality.convertNumericQualityToString
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

private const val TAG = "SleepNightAdapter"

class SleepNightAdapter: RecyclerView.Adapter<ViewHolder>() {
    var data = listOf<SleepNight>()
        set(value) {
            Log.i(TAG, "Calling set field: ${value}")
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        Log.i(TAG, "Setting new data: ${item.sleepQuality.toString()}")

        val res = holder.itemView.context.resources
        holder.bind(holder, item, res)
    }
}

class ViewHolder private constructor(binding: ListItemSleepNightBinding): RecyclerView.ViewHolder(binding.root) {

    val sleepLength: TextView = binding.sleepLength
    val quality: TextView = binding.qualityString
    val qualityImage: ImageView = binding.qualityImage

    fun bind(holder: ViewHolder, item: SleepNight, res: Resources) {
        holder.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
        holder.quality.text = convertNumericQualityToString(item.sleepQuality, res)
        holder.qualityImage.setImageResource(
                when (item.sleepQuality) {
                    0 -> R.drawable.ic_sleep_0
                    1 -> R.drawable.ic_sleep_1
                    2 -> R.drawable.ic_sleep_2
                    3 -> R.drawable.ic_sleep_3
                    4 -> R.drawable.ic_sleep_4
                    5 -> R.drawable.ic_sleep_5
                    else -> R.drawable.ic_sleep_active
                }
        )
    }

    // Use companion object to self initialize
    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ListItemSleepNightBinding>(layoutInflater,
                    R.layout.list_item_sleep_night, parent, false)

            Log.i(TAG, "ItemViewHolder receive view")
            return ViewHolder(binding)
        }
    }
}
