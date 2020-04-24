package org.kabiri.android.usbterminal.model

import android.content.Context
import android.text.style.TextAppearanceSpan

/**
 * Created by Ali Kabiri on 21.04.20.
 */
data class OutputText(
    val text: String,
    val type: OutputType
) {

    enum class OutputType {
        TYPE_NORMAL,
        TYPE_INFO,
        TYPE_ERROR
    }

    fun getAppearance(context: Context): TextAppearanceSpan {
        return when (this.type) {
            OutputType.TYPE_NORMAL ->
                TextAppearanceSpan(context, android.R.style.TextAppearance_Material)
            OutputType.TYPE_INFO ->
                TextAppearanceSpan(context, android.R.style.TextAppearance_Material_Small)
            OutputType.TYPE_ERROR ->
                TextAppearanceSpan(context, android.R.style.TextAppearance_Material_Large)
        }
    }
}