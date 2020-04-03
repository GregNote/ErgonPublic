package pl.garedgame.game.extensions

import java.nio.ByteBuffer

fun ByteBuffer.putString(string: String): ByteBuffer {
    val stringArray = string.toByteArray(Charsets.UTF_8)
    putInt(stringArray.size)
    put(stringArray)
    return this
}

fun ByteBuffer.getString(): String {
    val array = ByteArray(this.int)
    get(array)
    return String(array)
}
