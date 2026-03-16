package com.inversioncoach.app.recording

internal class OrderedFrameBuffer<T> {
    private val byIndex = mutableMapOf<Int, T>()

    fun put(index: Int, value: T) {
        byIndex[index] = value
    }

    fun pop(index: Int): T? = byIndex.remove(index)

    fun size(): Int = byIndex.size
}
