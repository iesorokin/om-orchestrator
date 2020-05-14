package ru.iesorokin.payment.orchestrator.output.client.payment

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.enums.PaymentTypeEnum
import ru.iesorokin.payment.orchestrator.output.client.dto.SberbankRefundRequest
import java.math.BigDecimal

/**
 * Sberbank microservice client.
 */
@Component
class SberbankClient(
        private val sberbankRestTemplate: RestTemplate,
        @Value("\${sberbank.availabilityUrl}")
        private val availabilityUrl: String,
        @Value("\${payment.sberbank.urlTransactionRefund}")
        private val urlTransactionRefund: String
) : PaymentTypeClient(PaymentTypeEnum.SBERBANK, sberbankRestTemplate, availabilityUrl) {

    //Not implemented yet
    override fun isPaymentClientAvailable(
            deliveryMode: String,
            customerAuth: Boolean,
            customerType: String,
            customerReliability: Int,
            amount: BigDecimal): Boolean {
        return true
    }

    fun refund(orderId: String, storeId: Int, refundAmount: BigDecimal) {
        val request = SberbankRefundRequest(storeId, refundAmount)
        sberbankRestTemplate.postForObject(urlTransactionRefund, request, Void::class.java, orderId)
    }

}
