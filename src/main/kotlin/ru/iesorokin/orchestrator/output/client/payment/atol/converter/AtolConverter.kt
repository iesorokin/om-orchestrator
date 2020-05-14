package ru.iesorokin.orchestrator.output.client.payment.atol.converter

import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.orchestrator.core.domain.AtolGiveAwayProduct
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.PaymentTaskFiscalDataLine
import ru.iesorokin.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.domain.SolutionCustomer
import ru.iesorokin.orchestrator.core.domain.SupplierInfo
import ru.iesorokin.orchestrator.core.enums.SupplierType
import ru.iesorokin.orchestrator.core.enums.TaskType
import ru.iesorokin.orchestrator.core.exception.EmptyFieldException
import ru.iesorokin.orchestrator.output.client.dto.AtolGiveAwayRegisterRequest
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequest
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequestAgentInfo
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequestPayer
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequestProduct
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequestSupplier
import ru.iesorokin.orchestrator.output.client.sum
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime

private val UTC_ZONE = ZoneId.of("UTC")
private val log = KotlinLogging.logger { }

@Component
class AtolConverter {

    fun convertTaskAndSolutionToSaleRequest(
            task: PaymentTask, solution: Solution, correlationKey: String? = null
    ): AtolRegisterRequest {
        val products = task.lines.mapNotNull { it.toProduct(task) }
        return AtolRegisterRequest(
                taskId = task.taskId,
                correlationKey = correlationKey,
                dateTime = ZonedDateTime.now(UTC_ZONE),
                orderId = task.extOrderId ?: throw EmptyFieldException("field extOrderId empty in task ${task.taskId}"),
                storeId = task.executionStore
                        ?: throw EmptyFieldException("field executionStore empty in task ${task.taskId}"),
                payer = getCustomerPayer(solution).toRegisterRequestPayer(solution.solutionId),
                products = products,
                total = products.map { it.sum }.sum()
        )
    }

    fun convertTaskAndSolutionToRefundRequest(
            task: PaymentTask, solution: Solution, workflowId: String, correlationKey: String? = null
    ): AtolRegisterRequest {
        val requestProducts = getRequestProducts(task, workflowId)
        return AtolRegisterRequest(
                taskId = task.taskId,
                correlationKey = correlationKey,
                dateTime = ZonedDateTime.now(UTC_ZONE),
                orderId = task.extOrderId ?: throw EmptyFieldException("field extOrderId empty in task ${task.taskId}"),
                storeId = task.executionStore
                        ?: throw EmptyFieldException("field executionStore empty in task ${task.taskId}"),
                payer = getCustomerPayer(solution).toRegisterRequestPayer(solution.solutionId),
                products = requestProducts,
                total = requestProducts.map { it.sum }.sum()
        )
    }

    fun convertAtolGiveAwayToAtolGiveAwayRequest(atolGiveAway: AtolGiveAway) = AtolGiveAwayRegisterRequest(
            taskId = atolGiveAway.taskId,
            giveAwayId = atolGiveAway.giveAwayId,
            correlationKey = atolGiveAway.correlationKey,
            processInstanceId = atolGiveAway.processInstanceId, //todo: delete after 01.04.2020 - useless variable
            dateTime = ZonedDateTime.of(atolGiveAway.dateTime, UTC_ZONE),
            orderId = atolGiveAway.orderId,
            storeId = atolGiveAway.storeId,
            payer = AtolRegisterRequestPayer(
                    email = atolGiveAway.payer.email,
                    phone = atolGiveAway.payer.phone
            ),
            products = atolGiveAway.products.map { it.toAtolRegisterRequestProduct() },
            total = atolGiveAway.total
    )

    private fun AtolGiveAwayProduct.toAtolRegisterRequestProduct() = AtolRegisterRequestProduct(
            name = this.name,
            price = this.price,
            quantity = this.quantity,
            sum = this.sum,
            tax = this.tax,
            supplierInfo = this.supplierInfo?.toAtolRegisterRequestSupplier(),
            agentInfo = this.agentInfo?.toAtolRegisterRequestAgentInfo()
    )

    private fun getCustomerPayer(solution: Solution) =
            solution.customers
                    ?.firstOrNull { it.roles?.contains("PAYER") ?: false }
                    ?: throw EmptyFieldException("customer with role Payer not found in solution ${solution.solutionId}")

    private fun SolutionCustomer.toRegisterRequestPayer(solutionId: String) =
            AtolRegisterRequestPayer(
                    email = this.email,
                    phone = this.phone?.primary
                            ?: throw EmptyFieldException("field phone primary empty in task in solution $solutionId")
            )


    private fun PaymentTaskLine.toProduct(task: PaymentTask) =
            if (this.confirmedQuantity != BigDecimal.ZERO) {
                log.info { "toProduct ${this.itemCode} taskType is ${task.taskType} and quantity will be ${this.confirmedQuantity} or ${this.quantity}" }
                AtolRegisterRequestProduct(
                        name = this.itemName
                                ?: throw EmptyFieldException("field itemName empty in task ${task.taskId}"),
                        price = this.unitAmountIncludingVat,
                        quantity = if (task.taskType == TaskType.POD_POST_PAYMENT.name) this.confirmedQuantity else this.quantity,
                        sum = (this.unitAmountIncludingVat * this.confirmedQuantity).setScale(2, RoundingMode.HALF_UP),
                        tax = this.taxRate ?: BigDecimal(20)
                )
            } else {
                null
            }

    private fun getRequestProducts(task: PaymentTask, workflowId: String): List<AtolRegisterRequestProduct> {
        val taskByExtLineIds = task.lines.map { it.extLineId to it }.toMap()
        val refundStatusItems = task.refundStatusList
                ?.asSequence()
                ?.firstOrNull { it.refundWorkflowId == workflowId }
                ?.lines
        if (refundStatusItems == null || refundStatusItems.isEmpty()) {
            throw EmptyFieldException("Can't find in task ${task.taskId} refundStatusList line with workflowId $workflowId")
        }
        return refundStatusItems.asSequence()
                .filter { it.quantity != BigDecimal.ZERO }
                .map { it.toProduct(taskByExtLineIds, task.taskId) }
                .toList()
    }

    private fun PaymentTaskFiscalDataLine.toProduct(taskByExtLineIds: Map<String?, PaymentTaskLine>,
                                                    taskId: String): AtolRegisterRequestProduct {
        val taskLine = taskByExtLineIds[this.extLineId]
        return AtolRegisterRequestProduct(
                name = taskLine?.itemName ?: throw EmptyFieldException("field itemName empty in task $taskId"),
                price = this.unitAmountIncludingVat ?: throw EmptyFieldException("field price empty in line $taskId"),
                quantity = this.quantity,
                sum = (this.unitAmountIncludingVat * this.quantity).setScale(2, RoundingMode.HALF_UP),
                tax = taskLine.taxRate ?: BigDecimal(20)
        )
    }
}

private fun SupplierType.toAtolRegisterRequestAgentInfo(): AtolRegisterRequestAgentInfo =
        AtolRegisterRequestAgentInfo(
                type = this
        )

private fun SupplierInfo.toAtolRegisterRequestSupplier(): AtolRegisterRequestSupplier =
        AtolRegisterRequestSupplier(
                name = name,
                inn = inn,
                phones = phones
        )