package pl.garedgame.game.multiplayer

import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class Host(private val listener: Listener) : Thread("Host") {

    private val connections = mutableSetOf<Connection>()
    private var socketServer: ServerSocket? = null

    init {
        start()
    }

    override fun run() {
        val pool = Executors.newFixedThreadPool(8)
        socketServer = ServerSocket(NetUtils.PORT)
        listener.onStart()
        try {
            while (true) {
                socketServer?.let {
                    pool.execute(Connection(it.accept(), object : Connection.Listener {
                        override fun onConnectionOpen(connection: Connection) {
                            if (connections.add(connection)) {
                                listener.onOpen(connection)
                            }
                        }
                        override fun onCommand(connection: Connection, data: Command.Data) {
                            listener.onCommand(connection, data)
                        }
                        override fun onError(error: String) {
                            listener.onError(error)
                        }
                        override fun onConnectionClose(connection: Connection) {
                            connections.remove(connection)
                            listener.onClose(connection)
                        }
                    }))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onError(e.localizedMessage ?: "Null Localized Message")
        }
    }

    fun broadcast(byteBuffer: ByteBuffer, data: Command.Data) {
        data.parse(byteBuffer)
        for (connection in connections) connection.send(byteBuffer)
    }

    fun close() {
        socketServer?.close()
        socketServer = null
        interrupt()
    }

    interface Listener {
        fun onStart()
        fun onOpen(connection: Connection)
        fun onCommand(connection: Connection, data: Command.Data)
        fun onClose(connection: Connection)
        fun onError(error: String)
    }
}
