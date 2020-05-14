package ru.iesorokin.payment.orchestrator.output.client.payment.atol

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.exception.EmptyAtolResponseException
import ru.iesorokin.payment.orchestrator.output.client.dto.AtolRegisterResponse
import ru.iesorokin.payment.orchestrator.output.client.payment.atol.converter.AtolConverter

@Component
@RefreshScope
class AtolClient(private val restTemplateAtol: RestTemplate,
                 private val atolConverter: AtolConverter,
                 @Value("\${payment.atol.urlRegisterSale}") private val urlRegisterSale: String,
                 @Value("\${payment.atol.urlRegisterRefund}") private val urlRegisterRefund: String,
                 @Value("\${payment.atol.urlRegisterGiveAway}") private val urlRegisterGiveAway: String) {
    fun registerSale(task: PaymentTask, solution: Solution, correlationKey: String? = null): String {
        val request = atolConverter.convertTaskAndSolutionToSaleRequest(task, solution, correlationKey)
        val response = restTemplateAtol.postForObject(urlRegisterSale, request, AtolRegisterResponse::class.java)
        return response?.atolId
                ?: throw EmptyAtolResponseException("atol registration for task ${task.taskId} and solution ${solution.solutionId} not correct")
    }

    fun registerRefund(
            task: PaymentTask, solution: Solution, workflowId: String, correlationKey: String? = null
    ): String {
        val request = atolConverter.convertTaskAndSolutionToRefundRequest(
                task, solution, workflowId, correlationKey
        )
        val response = restTemplateAtol.postForObject(urlRegisterRefund, request, AtolRegisterResponse::class.java)
        return response?.atolId
                ?: throw EmptyAtolResponseException("atol refund for task ${task.taskId} and solution ${solution.solutionId} not correct")
    }

    fun registerGiveAway(giveAway: AtolGiveAway): String {
        val request = atolConverter.convertAtolGiveAwayToAtolGiveAwayRequest(giveAway)
        val response = restTemplateAtol.postForObject(urlRegisterGiveAway, request, AtolRegisterResponse::class.java)
        return response?.atolId
                ?: throw EmptyAtolResponseException("atol registration for task ${giveAway.taskId} and solution ${giveAway.orderId} not correct")
    }
}
