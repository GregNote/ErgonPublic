package pl.garedgame.game.multiplayer

import pl.garedgame.game.Game
import pl.garedgame.game.skirmish.Controller
import pl.garedgame.game.skirmish.Skirmish
import java.nio.ByteBuffer
import java.util.*
import java.util.regex.Pattern

class GameServer : Client.Listener, Host.Listener {

    private var listener: Listener? = null

    private val byteBuffer = ByteBuffer.allocate(NetUtils.PACKAGE_SIZE)
    private var client: Client? = null
    private var host: Host? = null
    private var serverLoop: ServerLoop? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun startClient(ip: String) {
        NetUtils.getWifiIpAddress().also { wifi ->
            if (wifi.isNotEmpty()) {
                if (client != null) throw Exception("Client already created")
                client = Client(ip, this)
                serverLoop = ServerLoop().apply { start() }
            } else {
                listener?.onNoWifi()
            }
        }
    }

    fun startHost() {
        NetUtils.getWifiIpAddress().also { wifi ->
            if (wifi.isNotEmpty()) {
                if (host != null) throw Exception("Host already created")
                host = Host(this)
            } else {
                listener?.onNoWifi()
            }
        }
    }

    fun close() {
        listener = null
        client?.close()
        client = null
        host?.close()
        host = null
        serverLoop?.interrupt()
        serverLoop = null
    }

    fun sendMessage(message: String) {
        sendData(Command.Message(message))
    }

    fun sendData(data: Command.Data) {
        serverLoop?.send(data)
    }

    override fun onStart() {
        startClient("localhost")
        listener?.onHostStart(NetUtils.getWifiIpAddress())
    }

    override fun onOpen(connection: Connection) {
        serverLoop?.send(connection, Command.SubmitName())
    }

    override fun onClose(connection: Connection) {
        Skirmish.removeController(connection.id)
    }

    override fun onCommand(connection: Connection, data: Command.Data) {
        when (data) {
            is Command.NameProposal -> {
                if (!Skirmish.hasControllerWithName(data.name)) {
                    serverLoop?.broadcast(Command.Message("${data.name} has joined"))
                    serverLoop?.send(connection, Command.NameAccepted(data.name))
                    serverLoop?.send(connection, Command.Message("Welcome ${data.name}"))
                    val newController = Controller().apply {
                        name = data.name
                    }
                    Skirmish.addController(connection.id, newController)
                    val json = Game.instance.gson.toJson(newController)
                    serverLoop?.send(connection, Command.UpdateWithJson("selfController", json))
                    serverLoop?.broadcast(Command.UpdateWithJson("newController", json))
                } else {
                    serverLoop?.send(connection, Command.SubmitName())
                }
            }
            else -> Skirmish.getControllerById(connection.id)?.apply {
                when (data) {
                    is Command.Message -> {
                        val matcher = Pattern.compile("/(.+)(.*)").matcher(data.msg)
                        if (matcher.matches()) {
                            serverLoop?.send(connection,
                                    Command.Message("no comments implementation"))
                        } else serverLoop?.broadcast(Command.Message(name, data.msg))
                    }
                    is Command.ChangePlayerName -> {
                        if (!Skirmish.hasControllerWithName(data.name)) {
                            serverLoop?.broadcast(Command.Message("$name is now ${data.name}"))
                            name = data.name
                            serverLoop?.send(connection, Command.NameAccepted(name))
                        } else {
                            serverLoop?.send(connection, Command.PlayerNameOccupied())
                        }
                    }
                    is Command.SendOrder -> {
                        slots.find { it.id == data.unitId }?.let {
                            it.moveTo(data.position)
                        }
                    }
                }
            }
        }
    }

    override fun onCommand(data: Command.Data) {
        listener?.onData(data)
    }

    override fun onOpen() {
        listener?.onClientStart()
    }

    override fun onClose() {
    }

    override fun onError(error: String) {
        host = null
        client = null
        listener?.onError(error)
    }

    inner class ServerLoop : Thread() {

        private val actions = ArrayDeque<(() -> Unit)>()

        private fun addRunnable(action: () -> Unit) {
            synchronized(actions) {
                actions.add(action)
            }
        }

        fun send(data: Command.Data) = addRunnable {
            client?.send(byteBuffer, data)
        }

        fun send(connection: Connection, data: Command.Data) = addRunnable {
            data.parse(byteBuffer)
            connection.send(byteBuffer)
        }

        fun broadcast(data: Command.Data) = addRunnable {
            host?.broadcast(byteBuffer, data)
        }

        override fun run() {
            client!!.start()
            var timestamp = System.currentTimeMillis()
            var since: Long
            while (true) {
                synchronized(actions) {
                    while (actions.isNotEmpty()) actions.pop().invoke()
                }
                since = System.currentTimeMillis() - timestamp
                timestamp = System.currentTimeMillis()
                val allUnits = Skirmish.getAllUnits()
                allUnits.forEach { it.update(since) }
                host?.broadcast(byteBuffer, Command.UpdatePositions(allUnits))
                try {
                    sleep(150)
                } catch (e: Exception) { }
            }
        }
    }

    interface Listener {
        fun onNoWifi()
        fun onHostStart(wifi: String)
        fun onClientStart()
        fun onError(error: String)
        fun onData(data: Command.Data)
    }
}
