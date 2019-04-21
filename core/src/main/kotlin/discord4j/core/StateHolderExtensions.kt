package discord4j.core


suspend fun StateHolder.awaitInvalidateStores(): Unit = invalidateStores().unit()

