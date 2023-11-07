package com.ibnzahoor98.geomute.fragment.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.ibnzahoor98.geomute.R


class DashboardGeofenceAdapter(items: List<DashboardGeofenceData>, listener: DashboardGeofenceAdapter.OnItemClickListener) :
    RecyclerView.Adapter<DashboardGeofenceAdapter.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(item: DashboardGeofenceData?)
    }
    private val items: List<DashboardGeofenceData>
    private val listener: OnItemClickListener



    init {
        this.items = items
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_geofence_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView
        private val lat: TextView
        private val long: TextView
        private val tag: TextView

        init {

            name = itemView.findViewById(R.id.dgv_name)
            lat  = itemView.findViewById(R.id.dgv_lat)
            long = itemView.findViewById(R.id.dgv_long)
            tag = itemView.findViewById(R.id.dgv_tag)

        }

        fun bind(item: DashboardGeofenceData, listener: OnItemClickListener) {
            name.setText(item.name)
            val item_name:String = item.name
            val item_radius:Double = item.radius
            val item_tag:String = item.tag
            var item_timestamp:Long = item.timestamp
            var item_coordinates: GeoPoint = item.coordinates
            var item_fenceId: String = item.fenceId
           // var item_muteMode: String = item.muteMode


            name.text = item_name
            lat.text = "Lat: " + String.format("%4f", item_coordinates.latitude)
            long.text = "Long: " + String.format("%4f", item_coordinates.longitude)
            tag.text = item_tag

            itemView.setOnClickListener { listener.onItemClick(item) }
        }
    }
}
