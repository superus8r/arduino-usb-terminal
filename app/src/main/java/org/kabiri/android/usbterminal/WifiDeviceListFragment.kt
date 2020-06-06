package org.kabiri.android.usbterminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.kabiri.android.usbterminal.databinding.FragmentWifiDeviceListBinding
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
        savedInstanceState: Bundle?): View? {

        // inflate the fragment layout using bindings.
        val binding = FragmentWifiDeviceListBinding
            .inflate(inflater, container, false)
        context ?: return binding.root
        val placeHolder = binding.tvPlaceHolderText
        val serverList = binding.wifiDeviceList

        val adapter = WifiDeviceAdapter()
        serverList.adapter = adapter

        subscribeUi(adapter, serverList, placeHolder) // update the list whenever the dataSet changes.

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.handleDeviceMode(requireContext())
    }

    private fun subscribeUi(adapter: WifiDeviceAdapter,
                            serverList: RecyclerView, placeholder: TextView) {
        viewModel.observeDevices()
        viewModel.wifiDevices.observe(viewLifecycleOwner, Observer { wifiDevices ->
            adapter.submitList(wifiDevices)
        })
        viewModel.showRemoteServers.observe(viewLifecycleOwner, Observer {
            // show a place holder for the list adapter saying that
            // this device is a server for itself!
            updatePlaceHolderVisibility(
                showRemoteServers = it, serverList = serverList, placeholder = placeholder)
        })
    }

    private fun updatePlaceHolderVisibility(showRemoteServers: Boolean,
                                            serverList: RecyclerView, placeholder: TextView) {
        if (showRemoteServers) {
            placeholder.visibility = View.GONE
            serverList.visibility = View.VISIBLE
        } else {
            placeholder.visibility = View.VISIBLE
            serverList.visibility = View.GONE
        }
    }
}