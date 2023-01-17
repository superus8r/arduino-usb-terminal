package org.kabiri.android.usbterminal.extensions

import android.widget.TextView

/**
 * Scroll the text view to last visible line.
 */
fun TextView.scrollToLastLine() {
    if (this.text.isNotBlank()) {
        // find internal layout for the position of the final line and subtract the height
        val scrollAmount = this.layout.getLineTop(this.lineCount) - this.height
        // if no need scroll needed, scrollAmount will be <=0
        if (scrollAmount > 0) this.scrollTo(0, scrollAmount)
        else this.scrollTo(0, 0)
    }
}