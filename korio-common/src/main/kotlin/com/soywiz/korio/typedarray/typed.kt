package com.soywiz.korio.typedarray

expect object Arrays {
	fun copyRangeTo(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int)
	fun copyRangeTo(src: ShortArray, srcPos: Int, dst: ShortArray, dstPos: Int, count: Int)
	fun copyRangeTo(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, count: Int)
	fun copyRangeTo(src: LongArray, srcPos: Int, dst: LongArray, dstPos: Int, count: Int)
	fun copyRangeTo(src: FloatArray, srcPos: Int, dst: FloatArray, dstPos: Int, count: Int)
	fun copyRangeTo(src: DoubleArray, srcPos: Int, dst: DoubleArray, dstPos: Int, count: Int)
	fun fill(src: ByteArray, value: Byte, from: Int, to: Int)
	fun fill(src: ShortArray, value: Short, from: Int, to: Int)
	fun fill(src: IntArray, value: Int, from: Int, to: Int)
	fun fill(src: FloatArray, value: Float, from: Int, to: Int)
	fun fill(src: DoubleArray, value: Double, from: Int, to: Int)
}

fun ByteArray.copyRangeTo(srcPos: Int, dst: ByteArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)
fun ShortArray.copyRangeTo(srcPos: Int, dst: ShortArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)
fun IntArray.copyRangeTo(srcPos: Int, dst: IntArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)
fun LongArray.copyRangeTo(srcPos: Int, dst: LongArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)
fun FloatArray.copyRangeTo(srcPos: Int, dst: FloatArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)
fun DoubleArray.copyRangeTo(srcPos: Int, dst: DoubleArray, dstPos: Int, count: Int): Unit = Arrays.copyRangeTo(this, srcPos, dst, dstPos, count)

fun ByteArray.fill(value: Byte, from: Int = 0, to: Int = this.size): Unit = Arrays.fill(this, value, from, to)
fun ShortArray.fill(value: Short, from: Int = 0, to: Int = this.size): Unit = Arrays.fill(this, value, from, to)
fun IntArray.fill(value: Int, from: Int = 0, to: Int = this.size): Unit = Arrays.fill(this, value, from, to)
fun FloatArray.fill(value: Float, from: Int = 0, to: Int = this.size): Unit = Arrays.fill(this, value, from, to)
fun DoubleArray.fill(value: Double, from: Int = 0, to: Int = this.size): Unit = Arrays.fill(this, value, from, to)

// @TODO: DEFAULT IMPLEMENTATION (not working)
//actual fun ByteArray.copyRangeTo(srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) {
//	for (n in 0 until count) dst[dstPos + n] = this[srcPos + n]
//}
//
//actual fun ByteArray.fill(value: Byte, from: Int, to: Int) {
//	for (n in from until to) this[n] = value
//}

/*
// @TODO: Implement JS Typed Buffers
expect class ArrayBuffer {
	companion object {
		//fun
	}
}

interface TypedArray {
}
*/