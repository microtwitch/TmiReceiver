package de.com.fdm.core

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class LoadBalancer @Autowired constructor(
    private val deduplicator: Deduplicator,
    private val meterRegistry: MeterRegistry,
){
    private val log = LoggerFactory.getLogger(LoadBalancer::class.java)
    private val readers = meterRegistry.gaugeCollectionSize(
        "tmiReceiver.reader.gauge",
        Tags.empty(),
        mutableListOf<Reader>()
    )!!

    fun initReaders() {
        for (i in 0..50) {
            val reader = Reader(meterRegistry)
            reader.setMessageCallback(this::handleMessage)
            reader.setJoinCallback(this::handleJoin)
            reader.setConnectCallback(this::handleConnect)

            reader.connect()
            readers.add(reader)
        }
    }

    private fun handleMessage(msg: TwitchMessage) {
        deduplicator.handleMessage(msg)
    }

    private fun handleJoin(channel: String) {
        log.info("Joined channel #{}", channel)
    }

    private fun handleConnect(readerId: String) {
        log.info("Connected reader {}", readerId)
    }

    fun joinChannel(channel: String) {
        for (reader in readers) {
            if (reader.readsChannel(channel)) {
                return
            }
        }

        for (reader in readers) {
            if (reader.hasCapacity()) {
                reader.join(channel)
                return
            }
        }

        Thread.sleep(500)
        joinChannel(channel)
    }
}
