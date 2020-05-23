package org.kabiri.android.usbterminal.ui.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter

/**
 * Created by Ali Kabiri on 23.05.20.
 */

@BindingAdapter("wifi device name")
fun bindDeviceName(textView: TextView, name: String) {
    textView.text = name
}