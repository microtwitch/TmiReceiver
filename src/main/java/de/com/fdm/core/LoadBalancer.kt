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
    private val log = LoggerFactory.getLogger(Reader::class.java)
    private val readers = meterRegistry.gaugeCollectionSize(
        "tmiReceiver.reader.gauge",
        Tags.empty(),
        mutableListOf<Reader>()
    )!!

    fun initReaders() {
       for (i in 0..50) {
           readers.add(Reader(deduplicator, meterRegistry))
       }
    }

    fun joinChannel(channel: String) {
        for (reader in readers) {
            if (reader.readsChannel(channel)) {
                return
            }
            if (reader.hasCapacity()) {
                reader.joinChannel(channel)
            }
        }
    }
}