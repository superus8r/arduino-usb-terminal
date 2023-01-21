package org.kabiri.android.usbterminal.extensions

import android.util.Log
import android.widget.TextView

/**
 * Scroll the text view to last visible line.
 */

private const val TAG = "TextViewExtensions"

fun TextView.scrollToLastLine() {
    try {
        if (this.text.isNotBlank()) {
            // find internal layout for the position of the final line and subtract the height
            val scrollAmount = this.layout.getLineTop(this.lineCount) - this.height
            // if no need scroll needed, scrollAmount will be <=0
            if (scrollAmount > 0) this.scrollTo(0, scrollAmount)
            else this.scrollTo(0, 0)
        }
    } catch (e: Exception) {
        Log.w(TAG, "Something went wrong when trying to autoscroll the output:\n${e.message}")
    }
}