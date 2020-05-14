package ru.iesorokin.orchestrator.core.service.refund

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.exception.SberbankRefundException
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.output.client.payment.SberbankClient

private const val FILE_PATH = "refundSberbankService/"

class RefundSberbankServiceTest {
    private val paymentTaskService = mock<PaymentTaskService>()
    private val sberbankClient = mock<SberbankClient>()
    private val refundSberbankService = RefundSberbankService(
            paymentTaskService = paymentTaskService,
            sberbankClient = sberbankClient
    )

    private val paymentTaskId = "77eb698b-644a-4505-90b7-8e48a93263fe"
    private val orderId = "181200000445"
    private val processInstanceId = "workflowId1"

    @Test
    fun `doRefund should calculateDepositAmount and send refund`() {
        val paymentTask = getObject<PaymentTask>("correct")
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(paymentTask)

        refundSberbankService.doRefund(processInstanceId, paymentTaskId, orderId)

        verify(sberbankClient).refund(orderId, paymentTask.executionStore!!, "2200.00".toBigDecimal())
    }

    @Test(expected = SberbankRefundException::class)
    fun `doRefund should should throw SberbankRefundException if client throw Exception`() {
        val paymentTask = getObject<PaymentTask>("correct")
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(paymentTask)
        doThrow(RuntimeException()).whenever(sberbankClient).refund(any(), any(), any())

        refundSberbankService.doRefund(processInstanceId, paymentTaskId, orderId)
    }

    @Test(expected = SberbankRefundException::class)
    fun `doRefund should should throw SberbankRefundException if refundLine's field unitAmountIncludingVat is null`() {
        val paymentTask = getObject<PaymentTask>("refund-unitAmountIncludingVat-is-null")
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(paymentTask)

        refundSberbankService.doRefund(processInstanceId, paymentTaskId, orderId)
    }

    private inline fun <reified T> getObject(prefix: String): T {
        return getFileAsObject<T>("$FILE_PATH${T::class.simpleName}-$prefix.json")
    }
}
