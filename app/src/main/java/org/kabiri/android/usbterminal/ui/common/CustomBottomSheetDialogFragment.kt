package org.kabiri.android.usbterminal.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.kabiri.android.usbterminal.R

open class CustomBottomSheetDialogFragment(
    private val canUserDragToCancel: Boolean = true,
): BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.ModalBottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    /**
     * Skip the "STATE_HALF_EXPANDED"
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
            sheet.behavior.isDraggable = canUserDragToCancel
        }
    }
}