package ru.iesorokin.payment.orchestrator.core.service.refund

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.payment.ATOL_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.CURRENT_PAYMENT_STATUS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement.*
import ru.iesorokin.payment.orchestrator.core.task.refund.RefundTpNetTask
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolRefundSuccessMessageServiceTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var atolRefundSuccessMessageService: AtolRefundSuccessMessageService
    @MockBean
    private lateinit var refundTpnetTask: RefundTpNetTask

    @get:Rule
    val thrown = ExpectedException.none()!!

    @Test
    fun `should successfully correlate received message and pass through the task`() {
        doNothing().`when`(refundTpnetTask).execute(any())
        val atolMessage = getFileAsObject<AtolTransactionMessage>("${ATOL_FILE_PATH}atol-message-success.json")
        val context = mapOf(
                ATOL_REFUND_ID to atolMessage.atolId as Any,
                CURRENT_PAYMENT_STATUS to "PAID"
        )

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
                .businessKey("correlationKey")
                .setVariables(context)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted


        atolRefundSuccessMessageService.processMessage(atolMessage)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
        rule.endProcess(processInstance)
    }

}
