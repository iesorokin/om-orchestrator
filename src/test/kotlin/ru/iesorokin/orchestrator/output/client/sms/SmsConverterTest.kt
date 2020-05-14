package ru.iesorokin.payment.orchestrator.output.client.sms

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.iesorokin.payment.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.payment.SMS_REQUEST_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.core.domain.InternalSmsRequest
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponse
import ru.iesorokin.payment.orchestrator.output.client.sms.dto.SendMultiSmsRequest

class SmsConverterTest {

    private val smsConverter = SmsConverter()

    @Test
    fun `should convert domain object to dto`() {
        val request = getFileAsObject<InternalSmsRequest>("${SMS_REQUEST_PATH}sms-internal-request.json")
        val expected = getFileAsObject<SendMultiSmsRequest>("${SMS_REQUEST_PATH}sms-request.json")

        val actual = smsConverter.smsRequestListConvert(listOf(request))

        assertEquals(listOf(expected), actual)
    }
}
