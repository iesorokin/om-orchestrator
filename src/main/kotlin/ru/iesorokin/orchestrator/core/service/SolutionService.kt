package ru.iesorokin.payment.orchestrator.core.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.enums.CustomerRole
import ru.iesorokin.payment.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.payment.orchestrator.core.exception.ErrorSendSmsMessageException
import ru.iesorokin.payment.orchestrator.output.client.solution.SolutionClient
import ru.iesorokin.payment.orchestrator.output.stream.sender.FiscalizationStatusSender

private val log = KotlinLogging.logger {}

@Service
class SolutionService(
        private val solutionClient: SolutionClient,
        private val fiscalizationStatusSender: FiscalizationStatusSender
) {

    fun getSolutionOrder(solutionId: String) = solutionClient.getSolutionOrder(solutionId)

    fun getCustomerWithRole(solution: Solution, customerRole: CustomerRole) =
            solution.customers?.first { it.roles != null && it.roles.contains(customerRole.name) }
                    ?: throw ErrorSendSmsMessageException("Customer with role ${CustomerRole.PAYER} not found " +
                            "in solution with id ${solution.solutionId}")

    fun sendFiscalizationStatus(paymentTaskId: String, solutionId: String, status: FiscalizationStatus) =
            fiscalizationStatusSender.sendFiscalizationStatus(paymentTaskId, solutionId, status)
}
