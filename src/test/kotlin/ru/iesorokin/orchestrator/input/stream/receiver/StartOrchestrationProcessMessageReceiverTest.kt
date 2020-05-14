package ru.iesorokin.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.MessagingException
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.service.CamundaService

class StartOrchestrationProcessMessageReceiverTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var camundaService: CamundaService

    private final val testProcessType = SBERBANK_REFUND_WITH_TPNET
    private final val testProcessBusinessKey = "1234"
    private final val testVariables = mapOf("testVar" to "testVal")

    @Test
    fun `receiver receives message successfully`() {
        val message = MessageBuilder.withPayload(jsonMessage).build()
        messagingSource.startOrchestrationProcessInput().send(message)

        verify(camundaService).startProcess(testProcessType, testProcessBusinessKey, testVariables)
    }

    @Test(expected = MessagingException::class)
    fun `receiver fails when consumes message with unknown process type`() {
        val message = MessageBuilder.withPayload(jsonMessageWithUnknownProcessType).build()
        messagingSource.startOrchestrationProcessInput().send(message)
    }

    @Test(expected = MessagingException::class)
    fun `receiver fails when consumes message with nullable process type`() {
        val message = MessageBuilder.withPayload(jsonMessageWithNullableProcessType).build()
        messagingSource.startOrchestrationProcessInput().send(message)
    }

    @Test
    fun `receiver should not do anything if count is greater than maxRetry`() {
        val message = MessageBuilder
                .withPayload(jsonMessage)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()
        messagingSource.startOrchestrationProcessInput().send(message)

        verifyZeroInteractions(camundaService)
    }

    @Language("JSON")
    val jsonMessage = """
            {
                    "businessProcessType": "${testProcessType.name}",
                    "businessKey": "$testProcessBusinessKey",
                    "variables": {
                        "testVar" : "testVal"
                    }
            }
        """.trimIndent()

    @Language("JSON")
    val jsonMessageWithUnknownProcessType = """
            {
                    "businessProcessType": "UNKNOWN_PROCESS_TYPE",
                    "businessKey": "$testProcessBusinessKey",
                    "variables": {
                        "testVar" : "testVal"
                    }
            }
        """.trimIndent()

    @Language("JSON")
    val jsonMessageWithNullableProcessType = """
            {
                    "businessProcessType": null,
                    "businessKey": "$testProcessBusinessKey",
                    "variables": {
                        "testVar" : "testVal"
                    }
            }
        """.trimIndent()
}
