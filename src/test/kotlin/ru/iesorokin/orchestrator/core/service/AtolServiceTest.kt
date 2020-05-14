package ru.iesorokin.payment.orchestrator.core.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.iesorokin.payment.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.payment.SOLUTION_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.core.domain.AgencyAgreement
import ru.iesorokin.payment.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.payment.orchestrator.core.domain.AtolGiveAwayProduct
import ru.iesorokin.payment.orchestrator.core.domain.AtolGivePayer
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.domain.SupplierInfo
import ru.iesorokin.payment.orchestrator.core.enums.SupplierType
import ru.iesorokin.payment.orchestrator.output.client.payment.atol.AtolClient
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AtolServiceTest {
    private val atolClient = mock<AtolClient>()
    private val paymentTaskService = mock<PaymentTaskService>()
    private val solutionService = mock<SolutionService>()
    private val opusService = mock<OpusService>()

    val solutionId = "solutionId"
    val paymentTaskId = "paymentTaskId"
    val processInstanceId = "processInstanceId"
    private val giveAwayId = "giveAwayId"
    val storeId = 49
    val expected = "atolId"
    private val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

    private val product1 =
            Product("itemCode1", "Отвертка", BigDecimal(1.1), BigDecimal(1), BigDecimal("1.10"), BigDecimal(20))

    private val product2 =
            Product("itemCode2", "Молоток", BigDecimal(2.2), BigDecimal(2), BigDecimal("4.40"), BigDecimal(20))

    private val product3 =
            Product("itemCode3", "Доставка", BigDecimal(3.3), BigDecimal(3), BigDecimal("9.90"), BigDecimal(10))

    private val product4 =
            Product("itemCode4", "Установка дверей-купе", BigDecimal(4.4), BigDecimal(4), BigDecimal("17.60"), BigDecimal(20))

    private val atolService = AtolService(
            atolClient = atolClient,
            paymentTaskService = paymentTaskService,
            solutionService = solutionService,
            opusService = opusService
    )

    @Test
    fun `registerAtolSale should return atolId`() {
        val atolId = "atolId"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

        whenever(atolClient.registerSale(task, solution)).thenReturn(atolId)

        val actualId = atolService.registerAtolSale(task, solution)

        assertThat(atolId).isEqualTo(actualId)
    }

    @Test
    fun `registerAtolRefund should return atolId`() {
        val atolId = "atolId"
        val workflowId = "workflowId"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

        whenever(atolClient.registerRefund(task, solution, workflowId)).thenReturn(atolId)

        val actualId = atolService.registerAtolRefund(task, solution, workflowId)

        assertThat(atolId).isEqualTo(actualId)
    }

    @Test
    fun `registerAtolGiveAway with argument processInstanceId should return atolId`() {
        val expectedAtolGiveAway = buildAtolGiveAway(processInstanceId = processInstanceId)
        val giveAway = buildPaymentTaskGiveAway(processInstanceId = processInstanceId)
        val giveAwaysFromTasks = listOf(giveAway.copy(processInstanceId = "any"), giveAway.copy(giveAwayId = giveAwayId, processInstanceId = null), giveAway)

        prepareWheneever(giveAwaysFromTasks)

        val atolId = atolService.registerAtolGiveAway(
                solutionId = solutionId,
                paymentTaskId = paymentTaskId,
                processInstanceId = processInstanceId,
                storeId = storeId
        )

        registerAtolGiveAwayChecks(atolId, expectedAtolGiveAway)
    }

    @Test
    fun `registerAtolGiveAway with argument giveAwayId should return atolId`() {
        val expectedAtolGiveAway = buildAtolGiveAway(giveAwayId = giveAwayId)
        val giveAway = buildPaymentTaskGiveAway(giveAwayId = giveAwayId)
        val giveAwaysFromTasks = listOf(giveAway.copy(processInstanceId = processInstanceId, giveAwayId = null), giveAway.copy(giveAwayId = "any"), giveAway)

        prepareWheneever(giveAwaysFromTasks)

        val atolId = atolService.registerAtolGiveAway(
                solutionId = solutionId,
                paymentTaskId = paymentTaskId,
                giveAwayId = giveAwayId,
                storeId = storeId
        )

        registerAtolGiveAwayChecks(atolId, expectedAtolGiveAway)
    }

    private fun registerAtolGiveAwayChecks(actualAtolId: String, expectedAtolGiveAway: AtolGiveAway) {
        val captor = argumentCaptor<AtolGiveAway>()
        assertEquals(expected, actualAtolId)
        verify(atolClient).registerGiveAway(captor.capture())
        assertThat(expectedAtolGiveAway).isEqualToIgnoringGivenFields(captor.firstValue, "dateTime")
        assertThat(expectedAtolGiveAway.dateTime.isBefore(captor.firstValue.dateTime)).isTrue()
    }


    private fun prepareWheneever(paymentTaskServiceGiveAways: Collection<GiveAway>) {
        whenever(solutionService.getSolutionOrder(solutionId)).thenReturn(solution)
        whenever(paymentTaskService.getGiveAways(paymentTaskId)).thenReturn(paymentTaskServiceGiveAways)

        whenever(opusService.getProductNameByProductId(setOf(product1.productId, product2.productId, product3.productId, product4.productId)))
                .thenReturn(
                        mapOf(
                                product1.productId to product1.productName,
                                product2.productId to product2.productName,
                                product3.productId to product3.productName,
                                product4.productId to product4.productName
                        ))

        whenever(opusService.getVatByProductId(setOf(product1.productId, product2.productId, product3.productId, product4.productId), storeId.toString()))
                .thenReturn(
                        mapOf(
                                product1.productId to product1.productTax,
                                product2.productId to product2.productTax,
                                product3.productId to product3.productTax,
                                product4.productId to product4.productTax
                        )
                )

        whenever(atolClient.registerGiveAway(any())).thenReturn(expected)

        whenever(paymentTaskService.getPaymentTask(paymentTaskId))
                .thenReturn(getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-with-delivery-and-service-lines.json"))
    }

    private fun buildPaymentTaskGiveAway(processInstanceId: String? = null, giveAwayId: String? = null): GiveAway {
        return GiveAway(
                created = LocalDateTime.of(2019, 10, 17, 14, 17, 30, 0),
                createdBy = "test",
                processInstanceId = processInstanceId,
                giveAwayId = giveAwayId,
                lines = listOf(
                        GiveAwayExternalLine(
                                extLineId = "extLineId1",
                                itemCode = product1.productId,
                                unitAmountIncludingVat = product1.productPrice,
                                lineId = "lineId1",
                                quantity = product1.productQuantity
                        ),
                        GiveAwayExternalLine(
                                extLineId = "extLineId2",
                                itemCode = product2.productId,
                                unitAmountIncludingVat = product2.productPrice,
                                lineId = "lineId2",
                                quantity = product2.productQuantity
                        ),
                        GiveAwayExternalLine(
                                extLineId = "extLineId3",
                                itemCode = product3.productId,
                                unitAmountIncludingVat = product3.productPrice,
                                lineId = "lineId3",
                                quantity = product3.productQuantity
                        ),
                        GiveAwayExternalLine(
                                extLineId = "extLineId4",
                                itemCode = product4.productId,
                                unitAmountIncludingVat = product4.productPrice,
                                lineId = "lineId4",
                                quantity = product4.productQuantity,
                                agencyAgreement = AgencyAgreement(
                                        supplierName = "supplierName4",
                                        supplierInn = "supplierInn4",
                                        supplierType = SupplierType.AGENT,
                                        supplierTaxRate = 20.toBigDecimal(),
                                        supplierPhones = listOf("phoneNumber")
                                )
                        )
                )
        )
    }

    private fun buildAtolGiveAway(processInstanceId: String? = null, giveAwayId: String? = null) = AtolGiveAway(
            taskId = paymentTaskId,
            processInstanceId = processInstanceId,
            giveAwayId = giveAwayId,
            dateTime = LocalDateTime.now(),
            orderId = solutionId,
            storeId = storeId,
            payer = AtolGivePayer(
                    email = "customer@test.test",
                    phone = "+71234567890"
            ),
            products = listOf(
                    AtolGiveAwayProduct(
                            name = product1.productName,
                            price = product1.productPrice,
                            quantity = product1.productQuantity,
                            sum = product1.productSum,
                            tax = product1.productTax
                    ),
                    AtolGiveAwayProduct(
                            name = product2.productName,
                            price = product2.productPrice,
                            quantity = product2.productQuantity,
                            sum = product2.productSum,
                            tax = product2.productTax
                    ),
                    AtolGiveAwayProduct(
                            name = product3.productName,
                            price = product3.productPrice,
                            quantity = product3.productQuantity,
                            sum = product3.productSum,
                            tax = product3.productTax
                    ),
                    AtolGiveAwayProduct(
                            name = product4.productName,
                            price = product4.productPrice,
                            quantity = product4.productQuantity,
                            sum = product4.productSum,
                            tax = product4.productTax,
                            supplierInfo = SupplierInfo(
                                    inn = "supplierInn4",
                                    name = "supplierName4",
                                    phones = listOf("phoneNumber")
                            ),
                            agentInfo = SupplierType.AGENT
                    )
            ),
            total = "33.00".toBigDecimal()
    )

    data class Product(
            val productId: String,
            val productName: String,
            val productPrice: BigDecimal,
            val productQuantity: BigDecimal,
            val productSum: BigDecimal,
            val productTax: BigDecimal
    )
}
