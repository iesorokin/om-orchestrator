package ru.iesorokin.orchestrator.core.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.BILLING_PAID_STATUS
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CorrelationMessage

class BillingServiceTest {
    private val camundaService = mock<CamundaService>()

    private val billingService = BillingService(camundaService)

    @Test
    fun `processBillingPaidStatusMessage works correctly`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val billingPaidStatusMessage = CorrelationMessage("billing-interaction", paymentTaskId)

        //When
        billingService.processBillingPaidStatusMessage(billingPaidStatusMessage)

        verify(camundaService).createMessageCorrelation(paymentTaskId, BILLING_PAID_STATUS)
    }
}