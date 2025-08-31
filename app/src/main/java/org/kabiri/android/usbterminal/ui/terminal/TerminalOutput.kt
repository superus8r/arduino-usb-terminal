package org.kabiri.android.usbterminal.ui.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        itemsIndexed(logs, key = { index, item -> index }) { _, item ->
            val color =
                when (item.type) {
                    OutputText.OutputType.TYPE_ERROR -> MaterialTheme.colorScheme.error
                    OutputText.OutputType.TYPE_INFO -> MaterialTheme.colorScheme.onBackground
                    else -> MaterialTheme.colorScheme.onBackground
                }
            Text(
                text = item.text.trimEnd('\n'),
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
