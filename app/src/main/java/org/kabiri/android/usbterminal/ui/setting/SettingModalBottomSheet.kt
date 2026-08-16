package org.kabiri.android.usbterminal.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.ui.common.CustomBottomSheetDialogFragment
import org.kabiri.android.usbterminal.ui.theme.UsbTerminalTheme

internal class SettingModalBottomSheet : CustomBottomSheetDialogFragment() {
    private val viewModel: SettingViewModel by lazy { ViewModelProvider(requireActivity())[SettingViewModel::class.java] }
    var onDismiss: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = layoutInflater.inflate(R.layout.sheet_content, container, true)
        val composeView = view?.findViewById<ComposeView>(R.id.composeViewSettingContent)

        composeView?.setContent {
            UsbTerminalTheme {
                SettingContent(
                    settingViewModel = viewModel,
                    onDismiss = {
                        onDismiss()
                        this.dismiss()
                    },
                )
            }
        }

        return view
    }
}
