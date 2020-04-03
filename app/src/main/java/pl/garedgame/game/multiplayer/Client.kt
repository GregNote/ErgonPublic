package pl.garedgame.game.multiplayer

import java.nio.ByteBuffer

class Client(private val address: String, private val listener: Listener) {

    var connection: Connection? = null
    var thread: Thread? = null

    fun start() {
        NetUtils.findHost(address)?.let { socket ->
            connection = Connection(socket, object : Connection.Listener {
                override fun onConnectionOpen(connection: Connection) {
                    listener.onOpen()
                }
                override fun onCommand(connection: Connection, data: Command.Data) {
                    listener.onCommand(data)
                }
                override fun onError(error: String) {
                    listener.onError(error)
                }
                override fun onConnectionClose(connection: Connection) {
                    listener.onClose()
                }
            })
            thread = Thread(connection).apply {
                name = "Client"
                start()
            }
        } ?: listener.onError("Cant connect")
    }

    fun send(byteBuffer: ByteBuffer, data: Command.Data) {
        data.parse(byteBuffer)
        connection?.send(byteBuffer)
    }

    fun close() {
        connection?.close()
        connection = null
        thread?.interrupt()
        thread = null
    }

    interface Listener {
        fun onOpen()
        fun onCommand(data: Command.Data)
        fun onError(error: String)
        fun onClose()
    }
}
