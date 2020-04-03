package pl.garedgame.game.multiplayer

import pl.garedgame.game.Vector2
import pl.garedgame.game.extensions.getString
import pl.garedgame.game.extensions.putString
import pl.garedgame.game.objects.GameUnit
import java.nio.ByteBuffer

class Command {

    enum class Id {
        SubmitName,
        NameProposal,
        NameAccepted,
        Message,
        ChangePlayerName,
        PlayerNameOccupied,
        UpdateWithJson,
        UpdatePositions,
        SendOrder,
    }

    class SubmitName : Data(Id.SubmitName)

    class NameProposal(val name: String) : Data(Id.NameProposal) {
        override fun prepare() {
            prepareBuffer.putString(name)
        }
    }

    class NameAccepted(val name: String) : Data(Id.NameAccepted) {
        override fun prepare() {
            prepareBuffer.putString(name)
        }
    }

    class Message(val name: String, val msg: String) : Data(Id.Message) {
        constructor(msg: String) : this("", msg)
        override fun prepare() {
            prepareBuffer.putString(name).putString(msg)
        }
    }

    class ChangePlayerName(val name: String) : Data(Id.ChangePlayerName) {
        override fun prepare() {
            prepareBuffer.putString(name)
        }
    }

    class PlayerNameOccupied : Data(Id.PlayerNameOccupied)

    class UpdateWithJson(val name: String, val data: String) : Data(Id.UpdateWithJson) {
        override fun prepare() {
            prepareBuffer.putString(name).putString(data)
        }
    }

    class UpdatePositions(val units: ArrayList<GameUnit>) : Data(Id.UpdatePositions) {
        override fun prepare() {
            prepareBuffer.apply {
                putInt(units.size)
                for (unit in units) {
                    putLong(unit.id)
                    putFloat(unit.orientation.pos.x)
                    putFloat(unit.orientation.pos.y)
                }
            }
        }
    }

    class SendOrder(val actionId: Int, val unitId: Long, val position: Vector2) : Data(Id.SendOrder) {
        override fun prepare() {
            prepareBuffer.apply {
                putInt(actionId)
                putLong(unitId)
                putFloat(position.x)
                putFloat(position.y)
            }
        }
    }

    companion object {

        private val prepareBuffer = ByteBuffer.allocate(NetUtils.PACKAGE_SIZE)

        fun parse(byteBuffer: ByteBuffer): Data? {
            return when (byteBuffer.int) {
                Id.SubmitName.ordinal -> {
                    SubmitName()
                }
                Id.NameProposal.ordinal -> {
                    val name = byteBuffer.getString()
                    NameProposal(name)
                }
                Id.NameAccepted.ordinal -> {
                    val name = byteBuffer.getString()
                    NameAccepted(name)
                }
                Id.Message.ordinal -> {
                    val name = byteBuffer.getString()
                    val msg = byteBuffer.getString()
                    Message(name, msg)
                }
                Id.ChangePlayerName.ordinal -> {
                    val name = byteBuffer.getString()
                    ChangePlayerName(name)
                }
                Id.PlayerNameOccupied.ordinal -> {
                    PlayerNameOccupied()
                }
                Id.UpdateWithJson.ordinal -> {
                    val name = byteBuffer.getString()
                    val data = byteBuffer.getString()
                    UpdateWithJson(name, data)
                }
                Id.UpdatePositions.ordinal -> {
                    val units = arrayListOf<GameUnit>()
                    for (i in 0 until byteBuffer.int) {
                        units.add(GameUnit().apply {
                            id = byteBuffer.long
                            orientation.pos.x = byteBuffer.float
                            orientation.pos.y = byteBuffer.float
                        })
                    }
                    UpdatePositions(units)
                }
                Id.SendOrder.ordinal -> {
                    SendOrder(byteBuffer.int, byteBuffer.long,
                            Vector2(byteBuffer.float, byteBuffer.float))
                }
                else -> null
            }
        }
    }

    abstract class Data(commandEnum: Id) {
        private val commandId: Int = commandEnum.ordinal
        open fun prepare() { }
        fun parse(byteBuffer: ByteBuffer) {
            prepareBuffer.clear()
            prepare()
            val position = prepareBuffer.position()
            byteBuffer.clear()
            byteBuffer.putInt(position)
            byteBuffer.putInt(commandId)
            byteBuffer.put(prepareBuffer.array(), 0, position)
        }
    }
}
