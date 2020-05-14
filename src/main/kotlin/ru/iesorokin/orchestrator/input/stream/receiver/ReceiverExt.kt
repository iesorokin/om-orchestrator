package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KLogger

internal const val X_DEATH_HEADER = "x-death"
internal const val ROUTE_KEY_HEADER = "routeTo"

internal fun Map<Any, Any?>?.getCount() = this?.get("count") as Long?

internal fun Map<Any, Any?>?.isDeadLetterCountOverflown(maxRetry: Long): Boolean {
    val count = this.getCount()
    if (count != null && count >= maxRetry) {
        /* todo should throw ImmediateAcknowledgeAmqpException, but fixed version not released yet
           https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_putting_it_all_together
           https://github.com/spring-cloud/spring-cloud-stream-binder-rabbit/issues/180
         */
        return true
    }
    return false
}

internal fun KLogger.deadLetterCountOverflownError(maxRetry: Long, inputQueueName: String, message: Any) {
    this.error {
        """Message consuming failed after $maxRetry attempts. For queue $inputQueueName and message: 
            |$message
            |""".trimMargin()
    }
}

internal fun KLogger.logInputMessage(inputQueueName: String, message: Any) {
    this.info {
        """Received in queue: $inputQueueName message: 
            |$message
            |""".trimMargin()
    }
}
