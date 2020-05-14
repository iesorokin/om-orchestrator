package ru.iesorokin.payment.orchestrator.core.service.prepayment

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.EventSubscription
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.extension.mockito.QueryMocks
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.PaymentTransactionMessage
import ru.iesorokin.utility.sleuthbase.MdcService

class PaymentTransactionServiceErrorTest {

    private val runtimeService = mock<RuntimeService>()
    private val camundaService = CamundaService(runtimeService)
    private val mdcService = mock<MdcService>()
    private val paymentTransactionService = PaymentTransactionService(camundaService, mdcService)

    private val processInstance = mock<ProcessInstance>()
    private val eventSubscription = mock<EventSubscription>()

    @Before
    fun setUp() {
        QueryMocks.mockProcessInstanceQuery(runtimeService).singleResult(processInstance)
        whenever(processInstance.processInstanceId).thenReturn("processInstanceId")
    }

    @Test(expected = KotlinNullPointerException::class)
    fun `should fail when no event found`() {
        QueryMocks.mockEventSubscriptionQuery(runtimeService).singleResult(null)

        processMessage()
    }

    @Test(expected = ProcessEngineException::class)
    fun `should fail when event count more than 1`() {
        QueryMocks.mockEventSubscriptionQuery(runtimeService).singleResult(getEventWithProcessEngineException())

        processMessage()
    }

    @Test(expected = RuntimeException::class)
    fun `should fail when event cannot found`() {
        QueryMocks.mockEventSubscriptionQuery(runtimeService).singleResult(getEventWithRuntimeException())

        processMessage()
    }

    @Test(expected = RuntimeException::class)
    fun `should throw RuntimeException during event execution`() {
        QueryMocks.mockEventSubscriptionQuery(runtimeService).singleResult(eventSubscription)
        whenever(eventSubscription.eventName).thenReturn("HOLD")
        whenever(eventSubscription.executionId).thenReturn("executionId")
        whenever(runtimeService.messageEventReceived("HOLD", "executionId", null))
                .thenThrow(RuntimeException("error"))

        processMessage()

        verify(runtimeService).messageEventReceived("HOLD", "executionId", null)
    }

    @Test(expected = ProcessEngineException::class)
    fun `should throw ProcessEngineException during event execution`() {
        QueryMocks.mockEventSubscriptionQuery(runtimeService).singleResult(eventSubscription)
        whenever(eventSubscription.eventName).thenReturn("HOLD")
        whenever(eventSubscription.executionId).thenReturn("executionId")
        whenever(runtimeService.messageEventReceived("HOLD", "executionId", null))
                .thenThrow(ProcessEngineException("error"))

        processMessage()

        verify(runtimeService).messageEventReceived("HOLD", "executionId", null)
    }

    private fun processMessage() {
        paymentTransactionService.processMessage(
                PaymentTransactionMessage(
                        paymentTransaction = "paymentTransaction",
                        status = PaymentTransactionStatus.HOLD
                )
        )
    }

    private fun getEventWithProcessEngineException(): EventSubscription {
        throw ProcessEngineException("count more than 1")
    }

    private fun getEventWithRuntimeException(): EventSubscription {
        throw RuntimeException("error")
    }

}
