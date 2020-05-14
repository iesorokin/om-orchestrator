package ru.iesorokin.orchestrator.core.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.core.domain.InternalSmsRequest
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.Phone
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.domain.SolutionCustomer
import ru.iesorokin.orchestrator.output.client.payment.task.PaymentTaskClient
import ru.iesorokin.orchestrator.output.client.sms.SmsClient
import ru.iesorokin.orchestrator.output.client.solution.SolutionClient
import ru.iesorokin.orchestrator.output.stream.sender.FiscalizationStatusSender

class SmsServiceTest {
    private val solutionClient = mock<SolutionClient>()
    private val fiscalizationStatusSender = mock<FiscalizationStatusSender>()
    private val solutionService = SolutionService(solutionClient, fiscalizationStatusSender)
    private val paymentTaskClient = mock<PaymentTaskClient>()
    private val camundaService = mock<CamundaService>()
    private val paymentTaskService = PaymentTaskService(paymentTaskClient, camundaService)
    private val smsClient = mock<SmsClient>()
    private val smsService = SmsService(solutionService, paymentTaskService, smsClient)

    @get:Rule
    var thrown = ExpectedException.none()

    @Test
    fun `should send multi sms order confirmed if everything is ok`() {
        // Given
        val solutionId = "solutionId"
        val paymentTaskId = "87654321"
        val phone = "+79999999999"
        val storeId = "35"
        val customerName = "Andy"
        val customer = SolutionCustomer(
                name = customerName,
                roles = listOf("PAYER"),
                phone = Phone(primary = phone)
        )
        val solution = Solution(
                customers = listOf(customer),
                originStore = storeId,
                solutionId = solutionId
        )
        val event = REFUND_FOR_PREPAYMENT
        val appSender = PUZ_2
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        whenever(solutionClient.getSolutionOrder(solutionId)).thenReturn(solution)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task)

        // When
        smsService.sendMultiSms(paymentTaskId, "workflowId1", solutionId)

        // Then
        verify(smsClient, times(1)).sendMultiSms(
                listOf(InternalSmsRequest(
                        event,
                        phone,
                        NOW,
                        storeId.toInt(),
                        mapOf("customerName" to customerName,
                                SOLUTION_ID to "solutionId",
                                REFUND_AMOUNT to "46869.66"),
                        appSender
                ))
        )
    }
}
