package de.com.fdm.core

import okhttp3.WebSocketListener
import okhttp3.WebSocket
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.*
import kotlin.concurrent.timer
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import java.time.Instant
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory

class Reader constructor(
    private val meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(Reader::class.java)
    private lateinit var connectCallback: (readerId: String) -> Unit
    private lateinit var callback: (msg: TwitchMessage) -> Unit
    private lateinit var joinCallback: (channel: String) -> Unit
    private lateinit var socket: WebSocket
    private val id = UUID.randomUUID()
    private val recentMessages = HashMap<Instant, String>()
    private var isConnected = false
    private val channels = mutableMapOf<String, Boolean>()
    private var whenLastJoin: Instant

    private val channelGauge = meterRegistry.gauge(
        "tmiReceiver.channels.gauge", Tags.of("reader", id.toString()), AtomicInteger(0))!!

    init {
        whenLastJoin = Instant.now()
    }

    fun connect() {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url("wss://irc-ws.chat.twitch.tv:443")
                        .build()
        val listener = TwitchWebSocketListener()
        socket = client.newWebSocket(request, listener)

        timer("channelJoinStatusChecker", true, 1000, 1000) {
            for ((channel, isConnected) in channels) {
                if (!isConnected) {
                    socket.send("JOIN #$channel")
                }
            }
        }
    }

    fun setMessageCallback(callback: (msg: TwitchMessage) -> Unit) {
        this.callback = callback
    }

    fun setJoinCallback(callback: (channel: String) -> Unit) {
        this.joinCallback = callback
    }

    fun setConnectCallback(callback: (readerId: String) -> Unit) {
        this.connectCallback = callback;
    }

    fun readsChannel(channel: String) : Boolean{
        return channels.containsKey(channel)
    }

    fun hasCapacity() : Boolean {
        val timeSinceLastJoin = Duration.between(whenLastJoin, Instant.now()).toMillis()
        if ((recentMessages.size/5) < 100 &&  timeSinceLastJoin > 5000) {
            return true
        }

        return false
    }

    fun join(channel: String) {
        channels.put(channel, false)
        socket.send("JOIN #$channel")
    }

    private fun handlePrivMessage(msg: TwitchMessage) {
        val it = recentMessages.entries.iterator()
        while (it.hasNext()) {
            if (Duration.between(it.next().key, Instant.now()).toMillis() > 5000) {
                it.remove()
            }
        }

        recentMessages[Instant.now()] = msg.tags.getValue("id")
        callback.invoke(msg)
    }

    private inner class TwitchWebSocketListener : WebSocketListener() { 
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send("PASS asdf")
            webSocket.send("NICK justinfan6969")
            webSocket.send("CAP REQ :twitch.tv/tags")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            for (line in text.lines()) {
                if (line.isBlank()) {
                    continue
                }

                if (line.startsWith("PING")) {
                    webSocket.send("PONG :tmi.twitch.tv")
                }

                if (line == ":tmi.twitch.tv 376 justinfan6969 :>") {
                    isConnected = true
                    connectCallback.invoke(id.toString())
                }

                if (line.startsWith(":justinfan6969.tmi.twitch.tv") && line.endsWith("list")) {
                    handleJoin(line)
                }

                if (!line.startsWith(":justinfan6969") && !line.startsWith(":tmi.twitch.tv")) {
                    val twitchMessage = parseMessage(line)
                    if (twitchMessage != null) {
                        handlePrivMessage(twitchMessage)
                    }
                }
            }
        }

        private fun handleJoin(line: String) {
            val parts = line.split(" ")
            val channel = parts[3].removePrefix("#")
            channels[channel] = true
            channelGauge.incrementAndGet()
            joinCallback.invoke(channel)
        }

        private fun parseMessage(msg: String) : TwitchMessage? {
            val parts = msg.split(" ")


            when (parts[2]) {
                "PRIVMSG" -> {
                    val userName = parts[1].split("!")[0].removePrefix(":")
                    val message = msg.split(":")[2]
                    val channel = parts[3].removePrefix("#")

                    val tags = mutableMapOf<String, String>()
                    val rawTags = parts[0].split(";")

                    for (rawTag in rawTags) {
                        val tagParts = rawTag.split("=")
                        tags[tagParts[0]] = tagParts[1]
                    }


                    return TwitchMessage(channel, userName, message, tags.toMap())
                }
            }

            return null
        }
    }
}

