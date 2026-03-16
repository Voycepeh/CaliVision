package com.inversioncoach.app.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderedFrameBufferTest {
    @Test
    fun preservesOrderedEmissionWithOutOfOrderInputs() {
        val buffer = OrderedFrameBuffer<String>()
        buffer.put(2, "c")
        buffer.put(0, "a")
        buffer.put(1, "b")

        assertEquals("a", buffer.pop(0))
        assertEquals("b", buffer.pop(1))
        assertEquals("c", buffer.pop(2))
        assertNull(buffer.pop(3))
    }
}
