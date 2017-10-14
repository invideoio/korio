package com.soywiz.korio.net

import com.soywiz.korio.async.*
import com.soywiz.korio.coroutine.getCoroutineContext
import com.soywiz.korio.coroutine.korioSuspendCoroutine
import com.soywiz.korio.lang.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

class JvmAsyncSocketFactory : AsyncSocketFactory() {
	override suspend fun createClient(): AsyncClient = JvmAsyncClient()
	override suspend fun createServer(port: Int, host: String, backlog: Int): AsyncServer = JvmAsyncServer(port, host, backlog).apply { init() }
}

//private val newPool by lazy { Executors.newFixedThreadPool(1) }
//private val group by lazy { AsynchronousChannelGroup.withThreadPool(newPool) }

class JvmAsyncClient(private var sc: AsynchronousSocketChannel? = null) : AsyncClient {
	private val readQueue = AsyncThread()
	private val writeQueue = AsyncThread()

	//suspend override fun connect(host: String, port: Int): Unit = suspendCoroutineEL { c ->
	suspend override fun connect(host: String, port: Int): Unit = korioSuspendCoroutine { c ->
		sc?.close()
		sc = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(EventLoopExecutorService(c.context.eventLoop)))
		sc?.connect(InetSocketAddress(host, port), this, object : CompletionHandler<Void, AsyncClient> {
			override fun completed(result: Void?, attachment: AsyncClient): Unit = run { c.resume(Unit) }
			override fun failed(exc: Throwable, attachment: AsyncClient): Unit = run { c.resumeWithException(exc) }
		})
	}

	override val connected: Boolean get() = sc?.isOpen ?: false

	suspend override fun read(buffer: ByteArray, offset: Int, len: Int): Int = readQueue { _read(buffer, offset, len) }
	//suspend override fun read(buffer: ByteArray, offset: Int, len: Int): Int = _read(buffer, offset, len)

	suspend override fun write(buffer: ByteArray, offset: Int, len: Int): Unit = writeQueue {
		_write(buffer, offset, len)
	}

	//suspend private fun _read(buffer: ByteArray, offset: Int, len: Int): Int = suspendCoroutineEL { c ->
	suspend private fun _read(buffer: ByteArray, offset: Int, len: Int): Int = korioSuspendCoroutine { c ->
		if (sc == null) throw IOException("Not connected")
		val bb = ByteBuffer.wrap(buffer, offset, len)
		sc!!.read(bb, this, object : CompletionHandler<Int, AsyncClient> {
			override fun completed(result: Int, attachment: AsyncClient): Unit = c.resume(result)
			override fun failed(exc: Throwable, attachment: AsyncClient): Unit = c.resumeWithException(exc)
		})
	}

	suspend private fun _write(buffer: ByteArray, offset: Int, len: Int): Unit {
		_writeBufferFull(ByteBuffer.wrap(buffer, offset, len))
	}

	suspend private fun _writeBufferFull(bb: ByteBuffer) {
		while (bb.hasRemaining()) {
			_writeBufferPartial(bb)
		}
	}


	suspend private fun _writeBufferPartial(bb: ByteBuffer): Int = korioSuspendCoroutine { c ->
		if (sc == null) {
			throw IOException("Not connected")
		}
		AsyncClient.Stats.writeCountStart.incrementAndGet()
		sc!!.write(bb, this, object : CompletionHandler<Int, AsyncClient> {
			override fun completed(result: Int, attachment: AsyncClient) {
				AsyncClient.Stats.writeCountEnd.incrementAndGet()
				c.resume(result)
			}

			override fun failed(exc: Throwable, attachment: AsyncClient) {
				//println("write failed")
				AsyncClient.Stats.writeCountError.incrementAndGet()
				c.resumeWithException(exc)
			}
		})
	}

	suspend override fun close() {
		sc?.close()
		sc = null
	}
}

class JvmAsyncServer(override val requestPort: Int, override val host: String, override val backlog: Int = -1) : AsyncServer {
	val ssc = AsynchronousServerSocketChannel.open()

	suspend fun init(): Unit {
		ssc.bind(InetSocketAddress(host, requestPort), backlog)
		for (n in 0 until 100) {
			if (ssc.isOpen) break
			getCoroutineContext().eventLoop.sleep(50)
		}
	}

	override val port: Int get() = (ssc.localAddress as? InetSocketAddress)?.port ?: -1

	suspend override fun listen(handler: suspend (AsyncClient) -> Unit): Closeable {
		val ctx = getCoroutineContext()
		var running = true
		fun step() {
			if (!running) return

			ssc.accept(kotlin.Unit, object : CompletionHandler<AsynchronousSocketChannel, Unit> {
				override fun completed(result: AsynchronousSocketChannel, attachment: Unit) {
					spawnAndForget(ctx) {
						handler(JvmAsyncClient(result))
					}
					step()
				}

				override fun failed(exc: Throwable, attachment: Unit) = run {
					exc.printStackTrace()
					step()
				}
			})
		}
		step()

		return Closeable { running = false }
	}
}
