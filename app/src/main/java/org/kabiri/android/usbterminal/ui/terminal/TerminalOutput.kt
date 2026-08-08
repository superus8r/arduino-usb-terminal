package org.kabiri.android.usbterminal.ui.terminal

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.kabiri.android.usbterminal.R
import org.kabiri.android.usbterminal.model.OutputText

@Composable
internal fun TerminalOutput(
    logs: SnapshotStateList<OutputText>,
    autoScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new items arrive
    LaunchedEffect(logs.size, autoScroll) {
        if (autoScroll && logs.isNotEmpty()) {
            listState.scrollToItem(logs.lastIndex)
        }
    }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    // Concatenate all logs as plain text for copy action
    val allText = remember(logs.size) { logs.joinToString(separator = "") { it.text } }

    val longClickMessage = stringResource(R.string.copied_to_clipboard)

    LazyColumn(
        modifier =
            modifier.combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboard.setText(AnnotatedString(allText))
                    Toast.makeText(context, longClickMessage, Toast.LENGTH_SHORT).show()
                },
            ),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        itemsIndexed(logs, key = { index, _ -> index }) { index, item ->
            val isNormal = item.type == OutputText.OutputType.TYPE_NORMAL
            val isInfo = item.type == OutputText.OutputType.TYPE_INFO
            val isError = item.type == OutputText.OutputType.TYPE_ERROR

            val backgroundColor =
                when {
                    isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    isInfo -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }

            val textColor =
                when {
                    isError -> MaterialTheme.colorScheme.error
                    isInfo -> MaterialTheme.colorScheme.onBackground
                    else -> MaterialTheme.colorScheme.onBackground
                }

            val prefix =
                when {
                    isError -> "⚠️ "
                    isInfo -> "ℹ️ "
                    else -> ""
                }

            val fontFamily = if (isNormal) FontFamily.Monospace else FontFamily.Default
            val fontStyle = if (isInfo) FontStyle.Italic else FontStyle.Normal

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(horizontal = 12.dp, vertical = if (isNormal) 2.dp else 6.dp),
            ) {
                Text(
                    text = prefix + item.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = fontFamily,
                    fontStyle = fontStyle,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                )
            }
            if (index < logs.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    thickness = 1.dp,
                )
            }
        }
    }
}
