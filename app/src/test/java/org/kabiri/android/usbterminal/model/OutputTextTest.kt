package org.kabiri.android.usbterminal.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OutputTextTest {

    @Test
    fun testFields() {
        val dummyText = "does not matter"

        var sut = OutputText(
            text = dummyText,
            type = OutputText.OutputType.TYPE_NORMAL
        )
        assertThat(sut.text).isEqualTo(dummyText)
        assertThat(sut.type).isEqualTo(OutputText.OutputType.TYPE_NORMAL)

        sut = OutputText(
            text = dummyText,
            type = OutputText.OutputType.TYPE_INFO
        )
        assertThat(sut.text).isEqualTo(dummyText)
        assertThat(sut.type).isEqualTo(OutputText.OutputType.TYPE_INFO)

        sut = OutputText(
            text = dummyText,
            type = OutputText.OutputType.TYPE_ERROR
        )
        assertThat(sut.text).isEqualTo(dummyText)
        assertThat(sut.type).isEqualTo(OutputText.OutputType.TYPE_ERROR)
    }
}