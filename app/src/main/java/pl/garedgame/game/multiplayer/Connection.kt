package pl.garedgame.game.multiplayer

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer

class Connection(private val socket: Socket, private val listener: Listener) : Runnable {

    companion object {
        var gId = 0
    }

    val id = ++gId
    private lateinit var sIn: DataInputStream
    private lateinit var sOut: DataOutputStream

    override fun run() {
        try {
            sIn = DataInputStream(socket.getInputStream())
            sOut = DataOutputStream(socket.getOutputStream())
            listener.onConnectionOpen(this)

            val byteBuffer = ByteBuffer.allocate(NetUtils.PACKAGE_SIZE)
            val bytes = ByteArray(NetUtils.PACKAGE_SIZE)
            while (true) {
                val readBytesCount = sIn.read(bytes)
                if (-1 != readBytesCount) {
                    byteBuffer.put(bytes, 0, readBytesCount)
                    while (true) {
                        byteBuffer.flip()
                        if (canParseCommand(byteBuffer)) {
                            Command.parse(byteBuffer)?.let { listener.onCommand(this, it) }
                            if (byteBuffer.hasRemaining()) {
                                val remaining = byteBuffer.remaining()
                                byteBuffer.get(bytes, 0, remaining)
                                byteBuffer.clear()
                                byteBuffer.put(bytes, 0, remaining)
                                continue
                            }
                            byteBuffer.clear()
                        } else {
                            val limit = byteBuffer.limit()
                            byteBuffer.clear()
                            byteBuffer.position(limit)
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onError("Connection loop")
        } finally {
            try { socket.close() } catch (e: IOException) { }
            listener.onConnectionClose(this)
        }
    }

    private fun canParseCommand(byteBuffer: ByteBuffer): Boolean {
        var result = false
        if (byteBuffer.remaining() >= Int.SIZE_BYTES * 2) {
            byteBuffer.mark()
            val length = byteBuffer.int
            result = length <= byteBuffer.remaining()
            if (!result) byteBuffer.reset()
        }
        return result
    }

    fun send(byteBuffer: ByteBuffer) {
        sOut.write(byteBuffer.array(), 0, byteBuffer.position())
        sOut.flush()
    }

    fun close() {
        try { socket.close() } catch (e: IOException) { }
    }

    interface Listener {
        fun onConnectionOpen(connection: Connection)
        fun onCommand(connection: Connection, data: Command.Data)
        fun onError(error: String)
        fun onConnectionClose(connection: Connection)
    }
}
