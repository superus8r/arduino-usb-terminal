package org.kabiri.android.usbterminal.util

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ResourceProviderTest {
    private val mockContext = mockk<Context>()
    private val sut = ResourceProvider(mockContext)

    @Test
    fun `getString delegates to context getString`() {
        val resId = 123
        val expected = "test string"
        every { mockContext.getString(resId) } returns expected

        val actual = sut.getString(resId)

        assertThat(actual).isEqualTo(expected)
        verify { mockContext.getString(resId) }
    }
}