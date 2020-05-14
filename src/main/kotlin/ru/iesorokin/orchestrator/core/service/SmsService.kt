package ru.iesorokin.orchestrator.core.service

import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.domain.InternalSmsRequest
import ru.iesorokin.orchestrator.core.domain.SolutionCustomer
import ru.iesorokin.orchestrator.core.enums.CustomerRole
import ru.iesorokin.orchestrator.core.exception.ErrorSendSmsMessageException
import ru.iesorokin.orchestrator.output.client.sms.SmsClient
import java.math.BigDecimal

@Service
@RefreshScope
class SmsService(private val solutionService: SolutionService,
                 private val paymentTaskService: PaymentTaskService,
                 private val smsClient: SmsClient) {

    fun sendMultiSms(paymentTaskId: String, workflowId: String, solutionId: String) {
        val solution = solutionService.getSolutionOrder(solutionId)
        val customer = solutionService.getCustomerWithRole(solution, CustomerRole.PAYER)

        val receiver = getReceiver(customer)

        val paymentTask = paymentTaskService.getPaymentTask(paymentTaskId)

        val extLineIdToLineMap = paymentTask.lines.map { it.extLineId to it }.toMap()

        val refundAmount = paymentTask.refundStatusList
                ?.findLast { it.refundWorkflowId == workflowId }
                ?.lines
                ?.sumByDouble {
                    extLineIdToLineMap[it.extLineId]
                            ?.unitAmountIncludingVat
                            ?.multiply(it.quantity)
                            ?.setScale(2, BigDecimal.ROUND_HALF_UP)
                            ?.toDouble() ?: 0.toDouble()
                }?.toBigDecimal()
                ?.setScale(2, BigDecimal.ROUND_HALF_UP).toString()

        val storeId = paymentTask.executionStore

        val internalSmsRequest = InternalSmsRequest(
                REFUND_FOR_PREPAYMENT,
                receiver,
                NOW,
                storeId,
                mapOf(
                        CUSTOMER_NAME to customer.name,
                        SOLUTION_ID to solutionId,
                        REFUND_AMOUNT to refundAmount
                ),
                PUZ_2
        )

        smsClient.sendMultiSms(listOf(internalSmsRequest))
    }

    private fun getReceiver(customer: SolutionCustomer) = customer.phone?.primary
            ?: throw ErrorSendSmsMessageException("Phone primary is null in customer " +
                    "with role ${CustomerRole.PAYER}")
}
