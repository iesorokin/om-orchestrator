package ru.iesorokin.payment.orchestrator.output.client.sms

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.domain.InternalSmsRequest

@Component
@RefreshScope
class SmsClient(private val restTemplateSms: RestTemplate,
                @Value("\${sms.urlSendMultiSms}") private val urlSendMultiSms: String,
                @Value("\${sms.sendSms}") private val sendSms: Boolean,
                private val smsConverter: SmsConverter) {

    @Retryable(value = [Throwable::class],
            maxAttempts = 3,
            backoff = Backoff(delay = 60000))
    fun sendMultiSms(request: List<InternalSmsRequest>) {
        if (sendSms) {
            restTemplateSms.postForObject(
                    urlSendMultiSms,
                    smsConverter.smsRequestListConvert(request),
                    String::class.java)
        }
    }
}