package org.kabiri.android.usbterminal.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.kabiri.android.usbterminal.databinding.ListItemWifiDeviceBinding
import org.kabiri.android.usbterminal.model.WifiDevice

/**
 * Created by Ali Kabiri on 17.05.20.
 */
class WifiDeviceAdapter : ListAdapter<WifiDevice, RecyclerView.ViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = ListItemWifiDeviceBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = getItem(position)
        (holder as DeviceViewHolder).bind(device)
    }

    class DeviceViewHolder(
        private val binding: ListItemWifiDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.wifiDevice?.let { device ->
                    // TODO - connect to device
                    Log.d(this.javaClass.simpleName, "wifi device clicked: ${device.simpleName}")
                }
            }
        }

        fun bind(item: WifiDevice) {
            binding.apply {
                wifiDevice = item
                executePendingBindings()
            }
        }
    }
}

private class DeviceDiffCallback : DiffUtil.ItemCallback<WifiDevice>() {

    override fun areItemsTheSame(oldItem: WifiDevice, newItem: WifiDevice): Boolean {
        return oldItem.serviceName == newItem.serviceName
    }

    override fun areContentsTheSame(oldItem: WifiDevice, newItem: WifiDevice): Boolean {
        return oldItem == newItem
    }
}