package org.kabiri.android.usbterminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.kabiri.android.usbterminal.databinding.FragmentWifiDeviceListBinding
import org.kabiri.android.usbterminal.model.WifiDevice
import org.kabiri.android.usbterminal.ui.adapter.WifiDeviceAdapter
import org.kabiri.android.usbterminal.viewmodel.WifiDeviceListViewModel
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by Ali Kabiri on 23.05.20.
 */
class WifiDeviceListFragment: Fragment() {

    private val viewModel: WifiDeviceListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWifiDeviceListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = WifiDeviceAdapter()
        binding.wifiDeviceList.adapter = adapter
        subscribeUi(adapter)

        return binding.root
    }

    private fun subscribeUi(adapter: WifiDeviceAdapter) {
        viewModel.wifiDevices.observe(viewLifecycleOwner,
            Observer { wifiDevices -> adapter.submitList(wifiDevices) })
    }
}