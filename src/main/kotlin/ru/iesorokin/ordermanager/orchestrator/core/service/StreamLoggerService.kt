package ru.iesorokin.ordermanager.orchestrator.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogger
import org.springframework.stereotype.Component

@Component
class StreamLoggerService(private val logger: KLogger,
                          private val objectMapper: ObjectMapper) : KLogger by logger {
    internal fun inputMessage(inputQueueName: String, message: Any) = info {
        """Received in queue: $inputQueueName message: 
            |$message
            |""".trimMargin()
    }

    internal fun toJson(message: Any) = objectMapper.writeValueAsString(message)

    internal fun outputMessage(outputQueueName: String, msg: Any) = info {
        """Send to $outputQueueName message:
            ${toJson(msg)}
            """.trimMargin()
    }

    internal fun outputMessage(outputQueueName: String, msg: Any, header: String) = info {
        """Send to $outputQueueName with header $header message:
            ${toJson(msg)}
            """.trimMargin()
    }

    internal fun deadLetterCountOverflownError(maxRetry: Long, inputQueueName: String, message: Any) {
        error {
            """Message consuming failed after $maxRetry attempts. For queue $inputQueueName and message: 
            |${toJson(message)}
            |""".trimMargin()
        }
    }
}