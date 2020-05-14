package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.integration.support.MessageBuilder
import ru.iesorokin.payment.ATOL_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.getFileAsString
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.RECEIVE_ATOL_SUCCESS_RECEIVE_TASK
import ru.iesorokin.payment.orchestrator.core.service.prepayment.AtolTransactionMessageService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolTransactionMessageReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var atolTransactionMessageService: AtolTransactionMessageService

    private lateinit var atolMessageString: String
    private lateinit var atolMessage: AtolTransactionMessage
    private lateinit var processInstance: ProcessInstance

    @get:Rule
    val thrown = ExpectedException.none()!!

    @Before
    fun setUp() {
        atolMessageString = getFileAsString("${ATOL_FILE_PATH}atol-message-success.json")
        atolMessage = getFileAsObject<AtolTransactionMessage>("${ATOL_FILE_PATH}atol-message-success.json")
        val context = mapOf(
                ATOL_REGISTER_ID to atolMessage.atolId as Any
        )

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startBeforeActivity(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
                .setVariables(context)
                .execute()
    }

    @Test
    fun `should successfully correlate received message and pass through the task`() {
        val message = MessageBuilder.withPayload(atolMessageString)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 0.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedInput().send(message)

        verify(atolTransactionMessageService).processMessage(atolMessage)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }

    @Test
    fun `receive should not do anything if count equals to maxRetry`() {
        val message = MessageBuilder.withPayload(atolMessageString)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 10.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedInput().send(message)

        verifyZeroInteractions(atolTransactionMessageService)

        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}
