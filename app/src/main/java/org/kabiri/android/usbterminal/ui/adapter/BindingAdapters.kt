package org.kabiri.android.usbterminal.ui.adapter

import android.view.View
import androidx.databinding.BindingAdapter

/**
 * Created by Ali Kabiri on 23.05.20.
 * https://developer.android.com/topic/libraries/data-binding
 */
@BindingAdapter("app:goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility =
        if (visible) View.VISIBLE
        else View.GONE
}
