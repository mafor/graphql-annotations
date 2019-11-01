package com.chaosonic.graphql.library

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.DirectProcessor

@Component
class Observer {

    private companion object {
        @JvmStatic
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    private final val processor = DirectProcessor.create<String>()
    private final var sink = processor.sink()
    final val events = processor.share()
    final val logger = events.subscribe { log.info(it) }

    fun onBookAdded(id : String) {
        sink.next("Book added [$id]")
    }

    fun onBookRemoved(id : String) {
        sink.next("Book removed [$id]")
    }

    fun onAuthorAdded(id : String) {
        sink.next("Author added [$id]")
    }

    fun onAuthorRemoved(id : String) {
        sink.next("Author removed [$id]")
    }
}