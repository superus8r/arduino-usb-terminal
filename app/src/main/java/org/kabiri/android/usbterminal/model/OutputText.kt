package org.kabiri.android.usbterminal.model

/**
 * Created by Ali Kabiri on 21.04.20.
 */
data class OutputText(
    val text: String,
    val type: OutputType,
) {
    enum class OutputType {
        TYPE_NORMAL,
        TYPE_INFO,
        TYPE_ERROR,
    }
}
