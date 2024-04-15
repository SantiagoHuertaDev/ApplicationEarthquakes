package com.app.appearthquakes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private class EarthquakeAdapter(
    private val earthquakes: List<EarthquakeFeature>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<EarthquakeAdapter.EarthquakeViewHolder>() {
    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthquakeViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.xml.earthquake_item, parent, false)
        return EarthquakeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EarthquakeViewHolder, position: Int) {
        val earthquake = earthquakes[position]
        holder.bind(earthquake, listener)

        holder.textViewTitle.text = earthquake.properties.title
        holder.textViewMagnitude.text = earthquake.properties.mag.toString()
        holder.textViewDepth.text = earthquake.geometry.coordinates[2].toString()
        holder.textViewPlace.text = earthquake.properties.place
    }

    override fun getItemCount(): Int {
        return earthquakes.size
    }

    internal class EarthquakeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView
        var textViewMagnitude: TextView
        var textViewDepth: TextView
        var textViewPlace: TextView

        init {
            textViewTitle = itemView.findViewById(R.id.textViewTitle)
            textViewMagnitude = itemView.findViewById(R.id.textViewMagnitude)
            textViewDepth = itemView.findViewById(R.id.textViewDepth)
            textViewPlace = itemView.findViewById(R.id.textViewPlace)
        }

        fun bind(earthquake: EarthquakeFeature?, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(earthquake) }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(earthquake: EarthquakeFeature?)
    }
}
