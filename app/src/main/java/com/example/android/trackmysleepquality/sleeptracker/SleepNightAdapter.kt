package com.example.android.trackmysleepquality.sleeptracker

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
private const val TAG = "SleepNightAdapter"

class SleepNightAdapter: RecyclerView.Adapter<TextItemViewHolder>() {
    var data = listOf<SleepNight>()
        set(value) {
            Log.i(TAG, "Calling set field: ${value}")
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.text_item_view, parent, false) as TextView
        Log.i(TAG, "TextItemViewHolder receive view")
        return TextItemViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = data[position]
        Log.i(TAG, "Setting new data: ${item.sleepQuality.toString()}")
        holder.itemTextView.text = item.sleepQuality.toString()

        if (item.sleepQuality <= 1) {
            holder.itemTextView.setTextColor(Color.RED)
        } else {
            holder.itemTextView.setTextColor(Color.BLACK)
        }
    }
}

class TextItemViewHolder(val itemTextView: TextView): RecyclerView.ViewHolder(itemTextView)