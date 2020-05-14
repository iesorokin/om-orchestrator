package ru.iesorokin.orchestrator.output.client.payment

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import ru.iesorokin.orchestrator.core.enums.PaymentTypeEnum
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

/**
 * Abstract client for payment type microservice.
 *
 */
abstract class PaymentTypeClient(
        val type: PaymentTypeEnum,
        private val serviceRestTemplate: RestTemplate,
        private val availabilityUrl: String
) {

    /**
     * Check if payment type is available for store and delivery mode.
     *
     * @param store - store id
     * @param deliveryMode - delivery mode name
     * @return true if response from availabilityUrl has status == HttpStatus.OK, false otherwise
     */
    fun isPaymentTypeAvailable(
            store: Int, deliveryMode: String
    ): Boolean {
        log.info { "Requesting service ${type.name}: $availabilityUrl with storeId=$store, deliveryMode=$deliveryMode" }
        val paymentClientResponse = serviceRestTemplate.getForEntity(
                availabilityUrl,
                String::class.java,
                store,
                deliveryMode
        )
        log.info { "Service ${type.name} respond with ${paymentClientResponse.statusCode} code" }
        return paymentClientResponse.statusCode == HttpStatus.OK
    }

    abstract fun isPaymentClientAvailable(
            deliveryMode: String,
            customerAuth: Boolean,
            customerType: String,
            customerReliability: Int,
            amount: BigDecimal
    ): Boolean
}
