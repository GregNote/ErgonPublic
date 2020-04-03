package pl.garedgame.game.multiplayer

import android.content.Context
import android.net.wifi.WifiManager
import pl.garedgame.game.GameApplication
import pl.garedgame.game.util.SharedPrefs
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteOrder

class NetUtils {

    companion object {

        const val PORT = 8080
        const val PACKAGE_SIZE = 8192*4

        fun getWifiIpAddress(): String {
            val wifiManager = GameApplication.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var ipAddress = wifiManager.connectionInfo.ipAddress

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                ipAddress = Integer.reverseBytes(ipAddress)
            }
            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
            return try {
                val inetAddress = InetAddress.getByAddress(ipByteArray)
                inetAddress.hostAddress
            } catch (ex: Exception) {
                ex.printStackTrace()
                ""
            }
        }

        private fun tryConnect(ip: String): Socket? {
            try {
                Socket().apply {
                    connect(InetSocketAddress(ip, PORT))
                    return this
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun findHost(ip: String = ""): Socket? {
            if (ip.isNotEmpty()) {
                tryConnect(ip)?.let {
                    return it
                }
            } else {
                tryConnect(getWifiIpAddress())?.let {
                    return it
                } ?: tryConnect("localhost")?.let {
                    return it
                }
            }
            return null
        }

        fun loadLastIp(): String = SharedPrefs.getString("GAME_SERVER_KEY", "last_ip")

        fun saveLastIp(ip: String) {
            SharedPrefs.putString("GAME_SERVER_KEY", "last_ip", ip)
        }
    }
}
