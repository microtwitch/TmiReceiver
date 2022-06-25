package de.com.fdm.core

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger


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
    private val joins = meterRegistry.gauge(
        "tmiReceiver.joins.gauge", AtomicInteger(0))!!
    private val disconnects = meterRegistry.gauge(
        "tmiReceiver.disconnects.gauge", AtomicInteger(0))!!

    fun initReaders() {
        for (i in 0..50) {
            val reader = Reader(
                meterRegistry,
                this::handleConnect,
                this::handleJoin,
                this::handleMessage,
                this::handleDisconnect
            )

            reader.connect()
            readers.add(reader)
        }
    }

    private fun handleMessage(msg: TwitchMessage) {
        deduplicator.handleMessage(msg)
    }

    private fun handleJoin(channel: String) {
        joins.incrementAndGet()
    }

    private fun handleConnect(readerId: String) {
        log.info("Connected reader {}", readerId)
    }

    // TODO: rejoin channels that are lost
    private fun handleDisconnect(readerId: String) {
        log.info("Disconnected reader: {}", readerId)
        disconnects.incrementAndGet()

        val iter = readers.iterator()

        while (iter.hasNext()) {
            val reader = iter.next()
            if (reader.getId() == readerId) {
                iter.remove()
            }
        }

        val reader = Reader(
            meterRegistry,
            this::handleConnect,
            this::handleJoin,
            this::handleMessage,
            this::handleDisconnect
        )

        reader.connect()
        readers.add(reader)
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
