package org.kabiri.android.usbterminal.ui.terminal

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
        itemsIndexed(logs, key = { index, _ -> index }) { _, item ->
            val color =
                when (item.type) {
                    OutputText.OutputType.TYPE_ERROR -> MaterialTheme.colorScheme.error
                    OutputText.OutputType.TYPE_INFO -> MaterialTheme.colorScheme.onBackground
                    else -> MaterialTheme.colorScheme.onBackground
                }
            Text(
                text = item.text,
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
