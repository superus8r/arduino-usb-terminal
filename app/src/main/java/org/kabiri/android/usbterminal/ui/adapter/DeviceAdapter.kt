package org.kabiri.android.usbterminal.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.kabiri.android.usbterminal.model.Device

/**
 * Created by Ali Kabiri on 17.05.20.
 */
class DeviceAdapter(val deviceList: ArrayList<Device>) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder
    internal constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(android.R.id.text1)
        internal fun bind(device: Device) {
            tvName.text = device.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceAdapter.DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device)
    }
}