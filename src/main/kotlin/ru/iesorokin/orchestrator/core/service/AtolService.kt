package ru.iesorokin.payment.orchestrator.core.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.domain.AgencyAgreement
import ru.iesorokin.payment.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.payment.orchestrator.core.domain.AtolGiveAwayProduct
import ru.iesorokin.payment.orchestrator.core.domain.AtolGivePayer
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.domain.SolutionCustomer
import ru.iesorokin.payment.orchestrator.core.domain.SupplierInfo
import ru.iesorokin.payment.orchestrator.core.enums.CustomerRole
import ru.iesorokin.payment.orchestrator.core.enums.PaymentTaskLineType
import ru.iesorokin.payment.orchestrator.output.client.payment.atol.AtolClient
import java.math.BigDecimal
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

val DEFAULT_VAT = 20.toBigDecimal()

@Service
class AtolService(
        private val atolClient: AtolClient,
        private val paymentTaskService: PaymentTaskService,
        private val solutionService: SolutionService,
        private val opusService: OpusService
) {

    fun registerAtolSale(task: PaymentTask, solution: Solution, correlationKey: String? = null): String {
        log.info { "register atolSale ${task.taskId} with solution  ${solution.solutionId}" }
        return atolClient.registerSale(task, solution, correlationKey)
    }

    fun registerAtolRefund(
            task: PaymentTask, solution: Solution, workflowId: String, correlationKey: String? = null
    ): String {
        log.info { "register atolRefund ${task.taskId} with solution  ${solution.solutionId}" }
        return atolClient.registerRefund(task, solution, workflowId, correlationKey)
    }

    fun registerAtolGiveAway(
            solutionId: String, paymentTaskId: String, processInstanceId: String? = null, giveAwayId: String? = null,
            correlationKey: String? = null, storeId: Int
    ): String {
        val atolGiveAway = createAtolGiveAway(
                paymentTaskId = paymentTaskId,
                solutionId = solutionId,
                giveAwayId = giveAwayId,
                correlationKey = correlationKey,
                processInstanceId = processInstanceId,
                storeId = storeId
        )
        return atolClient.registerGiveAway(atolGiveAway)
    }

    private fun createAtolGiveAway(
            paymentTaskId: String, solutionId: String, giveAwayId: String?,
            processInstanceId: String?, storeId: Int, correlationKey: String? = null
    ): AtolGiveAway {
        val customer = getCustomerWithRolePayerOrReceiver(solutionId)
        val products = getGiveAwayProducts(
                paymentTaskId = paymentTaskId,
                giveAwayId = giveAwayId,
                processInstanceId = processInstanceId,
                storeId = storeId
        )

        return AtolGiveAway(
                taskId = paymentTaskId,
                giveAwayId = giveAwayId,
                correlationKey = correlationKey,
                processInstanceId = processInstanceId, //will be removed and replaced to processId after 01.04.2020
                dateTime = LocalDateTime.now(),
                orderId = solutionId,
                storeId = storeId,
                payer = AtolGivePayer(
                        email = customer.email,
                        phone = customer.phone?.primary!!
                ),
                products = products,
                total = products.calculateTotalSum()
        )
    }

    private fun getCustomerWithRolePayerOrReceiver(solutionId: String): SolutionCustomer {
        val customers = solutionService.getSolutionOrder(solutionId).customers

        val payer = customers.getCustomerWithRole(CustomerRole.PAYER)
        if (payer != null) {
            return payer
        }

        val receiver = customers.getCustomerWithRole(CustomerRole.RECEIVER)
        if (receiver != null) {
            return receiver
        }

        throw IllegalStateException("Not found customer with role PAYER or RECEIVER in solution $solutionId")
    }

    private fun getGiveAwayProducts(paymentTaskId: String, giveAwayId: String?, processInstanceId: String?, storeId: Int): Collection<AtolGiveAwayProduct> {
        val giveAwayLines = paymentTaskService.getGiveAways(paymentTaskId)

        val giveAwayExternalLines = giveAwayLines
                .find {
                    (giveAwayId != null && it.giveAwayId == giveAwayId) || (processInstanceId != null && it.processInstanceId == processInstanceId)
                }
                ?.lines ?: emptyList()

        val deliveryOrServiceLines = getDeliveryAndServiceLines(paymentTaskId)
        val deliveryOrServiceIds = deliveryOrServiceLines.map { it.itemCode }

        val productIds = giveAwayExternalLines
                .map { it.itemCode }
                .filter { !deliveryOrServiceIds.contains(it) }
                .toSet()
        val productNameByProductId = mutableMapOf<String, String>()
        val vatByProductId = mutableMapOf<String, BigDecimal>()

        if (productIds.isNotEmpty()) {
            productNameByProductId.putAll(opusService.getProductNameByProductId(productIds))
            vatByProductId.putAll(opusService.getVatByProductId(productIds, storeId.toString()))
        }

        // todo need fix. should take this data from master system
        if (deliveryOrServiceLines.isNotEmpty()) {
            deliveryOrServiceLines.forEach {
                productNameByProductId[it.itemCode] = it.itemName
                        ?: error("Product name with id ${it.itemCode} not found in payment task")
                vatByProductId[it.itemCode] = it.taxRate ?: getDefaultVat(it.itemCode, storeId)
            }
        }

        return giveAwayExternalLines
                .map {
                    AtolGiveAwayProduct(
                            name = productNameByProductId[it.itemCode]
                                    ?: error("Product name with id ${it.itemCode} not found"),
                            price = it.unitAmountIncludingVat,
                            quantity = it.quantity,
                            sum = (it.unitAmountIncludingVat * it.quantity).roundTwoDigit(),
                            tax = it.agencyAgreement?.supplierTaxRate ?: vatByProductId[it.itemCode]
                            ?: getDefaultVat(it.itemCode, storeId),
                            supplierInfo = it.agencyAgreement?.toSupplierInfo(),
                            agentInfo = it.agencyAgreement?.supplierType
                    )
                }
    }

    private fun getDefaultVat(productId: String, storeId: Int): BigDecimal {
        log.warn { "Vat for product with id $productId not found in store $storeId" }
        return DEFAULT_VAT
    }

    private fun Collection<SolutionCustomer>?.getCustomerWithRole(role: CustomerRole) =
            this?.firstOrNull { it.roles?.contains(role.name) ?: false }

    private fun Collection<AtolGiveAwayProduct>.calculateTotalSum() = this
            .asSequence()
            .map { it.sum }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .roundTwoDigit()

    private fun getDeliveryAndServiceLines(paymentTaskId: String) = paymentTaskService.getPaymentTask(paymentTaskId).lines.filter {
        it.lineType != null &&
                (it.lineType == PaymentTaskLineType.DELIVERY.name || it.lineType == PaymentTaskLineType.SERVICE.name)
    }
}

private fun AgencyAgreement.toSupplierInfo(): SupplierInfo? {
    return SupplierInfo(
            inn = supplierInn,
            name = supplierName,
            phones = supplierPhones
    )
}
