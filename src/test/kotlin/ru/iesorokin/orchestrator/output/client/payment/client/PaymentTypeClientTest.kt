package ru.iesorokin.payment.orchestrator.output.client.payment.client

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.enums.PaymentTypeEnum
import ru.iesorokin.payment.orchestrator.output.client.payment.PaymentTypeClient
import java.math.BigDecimal
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val TEST_AVAILABILITY_URL = "url"
private const val STORE_13 = 13
private const val DELIVERY_MODE_COURIER = "COURIER"

class TestSberbankClient(
        sberbankRestTemplate: RestTemplate,
        availabilityUrl: String
) : PaymentTypeClient(PaymentTypeEnum.SBERBANK, sberbankRestTemplate, availabilityUrl) {
    override fun isPaymentClientAvailable(deliveryMode: String, customerAuth: Boolean, customerType: String, customerReliability: Int, amount: BigDecimal): Boolean {
        //not implemented cause of test
        return true
    }
}

class PaymentTypeClientTest {

    private val serviceRestTemplate: RestTemplate = mock()

    private val testClient: TestSberbankClient = TestSberbankClient(serviceRestTemplate, TEST_AVAILABILITY_URL)

    @Test
    fun isAvailableReturnsTrueWhenStatusCodeIsOK() {
        // when
        whenever(serviceRestTemplate.getForEntity(
                TEST_AVAILABILITY_URL,
                String::class.java,
                STORE_13,
                DELIVERY_MODE_COURIER
        )).thenReturn(ResponseEntity(HttpStatus.OK))
        // then
        assertTrue(
                testClient.isPaymentTypeAvailable(STORE_13, DELIVERY_MODE_COURIER),
                "must return true if response status is ok"
        )
    }

    @Test
    fun isAvailableReturnsFalseWhenStatusCodeIsNotFound() {
        // when
        whenever(serviceRestTemplate.getForEntity(
                TEST_AVAILABILITY_URL,
                String::class.java,
                STORE_13,
                DELIVERY_MODE_COURIER
        )).thenReturn(ResponseEntity(HttpStatus.NOT_FOUND))
        // then
        assertFalse(
                testClient.isPaymentTypeAvailable(STORE_13, DELIVERY_MODE_COURIER),
                "must return true if response status is ok"
        )
    }
}
