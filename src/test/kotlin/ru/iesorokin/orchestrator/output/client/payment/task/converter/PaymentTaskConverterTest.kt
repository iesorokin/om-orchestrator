package ru.iesorokin.orchestrator.output.client.payment.task.converter

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.core.domain.EditLine
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.output.client.dto.PaymentTaskResponse
import ru.iesorokin.orchestrator.output.client.payment.task.dto.PatchLineRequest

class PaymentTaskConverterTest {

    private val paymentTaskConverter = PaymentTaskConverter()

    @Test
    fun `convertPaymentTaskResponseToPaymentTask should convert dto to domain model`() {
        val response = getFileAsObject<PaymentTaskResponse>("${PAYMENT_TASK_FILE_PATH}task-response-200.json")
        val expected = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-converted-from-response.json")

        val actual = paymentTaskConverter.convertPaymentTaskResponseToPaymentTask(response)

        assertEquals(expected, actual)
    }

    @Test
    fun `convertEditLinesToPathLinesRequest should correct convert EditLines to PathLinessRequest`() {
        val extLineIdOne = "extLineIdOne"
        val extLineIdTwo = "extLineIdTwo"
        val vat = 1.toBigDecimal()
        val quantity = 2.toBigDecimal()

        val editLines = listOf(
                EditLine(extLineId = extLineIdOne, unitAmountIncludingVat = vat),
                EditLine(extLineId = extLineIdTwo, confirmedQuantity = quantity)
        )

        val expected = listOf(
                PatchLineRequest(extLineId = extLineIdOne, unitAmountIncludingVat = vat, confirmedQuantity = null),
                PatchLineRequest(extLineId = extLineIdTwo, confirmedQuantity = quantity, unitAmountIncludingVat = null)
        )

        val actual = paymentTaskConverter.convertEditLinesToPathLinesRequest(editLines)
        assertEquals(expected, actual)
    }
}
