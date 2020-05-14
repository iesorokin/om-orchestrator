package ru.iesorokin.payment.orchestrator.core.service

import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.refEq
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringContains
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalDataLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskStatus.CONFIRMED
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskStatus.HOLD
import ru.iesorokin.payment.orchestrator.core.domain.RefundContext
import ru.iesorokin.payment.orchestrator.core.domain.RefundLine
import ru.iesorokin.payment.orchestrator.core.enums.Application.ORCHESTRATOR
import ru.iesorokin.payment.orchestrator.core.enums.ErrorCode
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.POD_AGENT
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.POD_POST_PAYMENT
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.SBERLINK_WITH_TPNET_DEPOSIT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.exception.InvalidTaskStatusException
import ru.iesorokin.payment.orchestrator.core.exception.InvalidUnitAmountIncludingVatException
import ru.iesorokin.payment.orchestrator.core.exception.LineNotFoundException
import ru.iesorokin.payment.orchestrator.core.exception.PaymentTaskNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.payment.task.PaymentTaskClient
import java.math.BigDecimal.ONE
import java.math.BigDecimal.TEN
import java.math.BigDecimal.ZERO
import java.time.LocalDateTime
import kotlin.test.assertNotNull


class PaymentTaskServiceTest {
    private val paymentTaskClient = mock<PaymentTaskClient>()
    private val camundaService = mock<CamundaService>()
    private val paymentTaskService = PaymentTaskService(
            paymentTaskClient = paymentTaskClient,
            camundaService = camundaService)

    @get:Rule
    var thrown = ExpectedException.none()

    @Test
    fun `updateWorkflowId should simple invoke client`() {
        val taskId = "12345678"
        val workflowId = "newWorkflowId"

        paymentTaskService.updateWorkflowId(taskId, workflowId)

        verify(paymentTaskClient, times(1)).updateWorkflowId(taskId, workflowId)
    }

    @Test
    fun `updateRegisterStatus should simple invoke client`() {
        val taskId = "12345678"
        val registerStatus = PaymentTaskRegisterStatus(
                uuid = "atol-uuid",
                ecrRegistrationNumber = "ecrRegistrationNumber",
                fiscalDocumentNumber = 31926072,
                fiscalStorageNumber = "fiscalStorageNumber",
                status = "DONE"
        )

        paymentTaskService.updateRegisterStatus(taskId, registerStatus)

        verify(paymentTaskClient, times(1)).updateRegisterStatus(taskId, registerStatus)
    }

    @Test
    fun `updateRefundStatusList should simple invoke client`() {
        val taskId = "12345678"
        val refundStatusList = listOf(
                PaymentTaskFiscalData(
                        refundWorkflowId = "refundWorkflowId1",
                        atolId = "atolId1",
                        lines = listOf(
                                PaymentTaskFiscalDataLine(
                                        extLineId = "extLineId1",
                                        lineId = "lineId1",
                                        quantity = ONE
                                )
                        )
                )
        )

        paymentTaskService.updateRefundStatusList(taskId, refundStatusList)

        verify(paymentTaskClient).updateRefundStatusList(taskId, refundStatusList)
    }

    @Test
    fun `getPaymentTask should return paymentTask`() {
        val taskId = "testTaskId"
        val expected = PaymentTask(
                taskId = taskId,
                taskStatus = "NEW",
                taskType = "SOLUTION",
                lines = listOf(PaymentTaskLine(
                        itemCode = "1",
                        lineStatus = "NEW",
                        quantity = ONE,
                        unitAmountIncludingVat = ONE,
                        confirmedQuantity = ONE,
                        depositQuantity = ONE
                ))
        )
        whenever(paymentTaskClient.getPaymentTask(taskId)).thenReturn(expected)

        val actual = paymentTaskService.getPaymentTask(taskId)

        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun `save refund lines`() {
        //Given
        val processInstanceId = "processInstanceId"
        val refundContext = refundContext()
        whenever(paymentTaskClient.saveRefundLines(processInstanceId, refundContext))
                .then { }

        //When
        paymentTaskService.saveRefundLines(processInstanceId, refundContext)

        //Then
        verify(paymentTaskClient, times(1)).saveRefundLines(processInstanceId, refundContext)
    }

    @Test
    fun `editLines should update lines without exception `() {
        //Given
        val taskId = "taskId"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 150.toBigDecimal())
        )

        val expected = PaymentTask(
                taskId = taskId,
                taskStatus = "HOLD",
                taskType = SBERLINK_WITH_TPNET_DEPOSIT.name,
                workflowId = "workflowId",
                lines = listOf(
                        PaymentTaskLine(
                                extLineId = "extLineIdOne",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 100.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE),
                        PaymentTaskLine(
                                extLineId = "extLineIdTwo",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 150.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE),
                        PaymentTaskLine(
                                extLineId = "extLineIdThree",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 200.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE)
                )
        )
        whenever(paymentTaskClient.getPaymentTask(taskId)).thenReturn(expected)
        doNothing().whenever(camundaService).updateVariableByProcessInstanceId("workflowId", "fullApprove", false)

        //When
        paymentTaskService.editLines(taskId, "system2", request)

        //Then
        val patchLineRequest = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 150.toBigDecimal())
        )
        verify(paymentTaskClient, times(1)).updateLines(taskId, "system2", patchLineRequest)
        verify(camundaService, times(1)).updateVariableByProcessInstanceId("workflowId", "fullApprove", false)
    }

    @Test
    fun `editLines should throw PaymentTaskNotFoundException when payment not found`() {
        val taskId = "taskId"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )

        doThrow(PaymentTaskNotFoundException("")).whenever(paymentTaskClient).getPaymentTask(taskId)
        thrown.expect(PaymentTaskNotFoundException::class.java)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test
    fun `editLines should throw IllegalArgumentException when unitAmountIncludingVat or confirmedQuantity not field`() {
        val taskId = "taskId"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne"),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )

        doThrow(PaymentTaskNotFoundException("")).whenever(paymentTaskClient).getPaymentTask(taskId)
        thrown.expect(IllegalArgumentException::class.java)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test
    fun `editLines should throw InvalidTaskStatusException because wrong task type`() {
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )
        val taskIdWithWrongType = "taskId"
        val taskWithWrongType = PaymentTask(
                taskId = taskIdWithWrongType,
                taskStatus = "NEW",
                taskType = "INCORRECT_TYPE",
                lines = paymentTaskLines())
        val taskToallowedTaskTypes = listOf(
                SBERLINK_WITH_TPNET_DEPOSIT to PaymentTask(
                        taskId = SBERLINK_WITH_TPNET_DEPOSIT.name,
                        workflowId = "someWorkflowId",
                        taskStatus = HOLD.name,
                        taskType = SBERLINK_WITH_TPNET_DEPOSIT.name,
                        lines = paymentTaskLines()),
                POD_POST_PAYMENT to PaymentTask(
                        taskId = POD_POST_PAYMENT.name,
                        taskStatus = CONFIRMED.name,
                        taskType = POD_POST_PAYMENT.name,
                        lines = paymentTaskLines()),
                POD_AGENT to PaymentTask(
                        taskId = POD_AGENT.name,
                        taskStatus = CONFIRMED.name,
                        taskType = POD_AGENT.name,
                        lines = paymentTaskLines())
        )


        // ok mapping
        taskToallowedTaskTypes.forEach {
            whenever(paymentTaskClient.getPaymentTask(it.first.name)).thenReturn(it.second)
        }

        // error mapping
        whenever(paymentTaskClient.getPaymentTask(taskIdWithWrongType)).thenReturn(taskWithWrongType)

        // then

        // ok
        taskToallowedTaskTypes.forEach {
            paymentTaskService.editLines(it.first.name, "system2", request)
        }

        // throw
        thrown.expect(InvalidTaskStatusException::class.java)
        thrown.expectMessage(StringContains.containsString(ErrorCode.TASK_STATUS_ERROR.errorMessage))

        paymentTaskService.editLines(taskIdWithWrongType, "system2", request)
    }

    @Test(expected = InvalidTaskStatusException::class)
    fun `editLines should throw IncorrectTaskStatusException because wrong task status for POD`() {
        val taskId = "pod"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )
        val task =
                PaymentTask(
                        taskId = POD_POST_PAYMENT.name,
                        taskStatus = HOLD.name, // must be CONFIRMED
                        taskType = POD_POST_PAYMENT.name,
                        lines = paymentTaskLines())

        whenever(paymentTaskClient.getPaymentTask(taskId = taskId)).thenReturn(task)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test(expected = InvalidTaskStatusException::class)
    fun `editLines should throw IncorrectTaskStatusException because wrong task status for DEPOSIT`() {
        val taskId = "deposit"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )
        val task =
                PaymentTask(
                        taskId = SBERLINK_WITH_TPNET_DEPOSIT.name,
                        taskStatus = CONFIRMED.name, // must be HOLD
                        taskType = SBERLINK_WITH_TPNET_DEPOSIT.name,
                        lines = paymentTaskLines())

        whenever(paymentTaskClient.getPaymentTask(taskId = taskId)).thenReturn(task)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test
    fun `editLines should throw LineNotFoundException when line not found in payment task`() {
        //Given
        val taskId = "taskId"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = 10.toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = 15.toBigDecimal())
        )

        val expected = PaymentTask(
                taskId = taskId,
                taskStatus = "HOLD",
                taskType = "SOLUTION",
                lines = listOf(
                        PaymentTaskLine(
                                extLineId = "extLineIdOne",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 100.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE)
                )
        )
        whenever(paymentTaskClient.getPaymentTask(taskId)).thenReturn(expected)
        thrown.expect(LineNotFoundException::class.java)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test
    fun `editLines should throw IncorrectUnitAmountIncludingVatException when vat in requst is negative or `() {
        //Given
        val taskId = "taskId"
        val request = listOf(
                EditLine(extLineId = "extLineIdOne", unitAmountIncludingVat = (-10).toBigDecimal()),
                EditLine(extLineId = "extLineIdTwo", unitAmountIncludingVat = ZERO),
                EditLine(extLineId = "extLineIdThree", unitAmountIncludingVat = 10.toBigDecimal())
        )

        val expected = PaymentTask(
                taskId = taskId,
                taskStatus = "HOLD",
                taskType = SBERLINK_WITH_TPNET_DEPOSIT.name,
                lines = listOf(
                        PaymentTaskLine(
                                extLineId = "extLineIdOne",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 100.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE),
                        PaymentTaskLine(
                                extLineId = "extLineIdTwo",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 100.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE),
                        PaymentTaskLine(
                                extLineId = "extLineIdThree",
                                itemCode = "1",
                                lineStatus = "NEW",
                                lineType = "DELIVERY",
                                quantity = ONE,
                                unitAmountIncludingVat = 10.toBigDecimal(),
                                confirmedQuantity = ONE,
                                depositQuantity = ONE)
                )
        )
        whenever(paymentTaskClient.getPaymentTask(taskId)).thenReturn(expected)
        thrown.expect(InvalidUnitAmountIncludingVatException::class.java)

        paymentTaskService.editLines(taskId, "system2", request)
    }

    @Test
    fun `createGiveAway should create giveAway for whole paymentTaskLines and send request`() {
        // given
        val extLineIdOne = "extLineIdOne"
        val extLineIdTwo = "extLineIdTwo"

        val paymentTaskLines = listOf(
                PaymentTaskLine(
                        extLineId = extLineIdOne,
                        itemCode = "1",
                        lineStatus = "NEW",
                        lineType = "DELIVERY",
                        quantity = 2.toBigDecimal(),
                        unitAmountIncludingVat = 100.toBigDecimal(),
                        confirmedQuantity = ONE,
                        depositQuantity = ONE),
                PaymentTaskLine(
                        extLineId = extLineIdTwo,
                        itemCode = "2",
                        lineStatus = "NEW",
                        lineType = "DELIVERY",
                        quantity = 2.toBigDecimal(),
                        unitAmountIncludingVat = 200.toBigDecimal(),
                        confirmedQuantity = ONE,
                        depositQuantity = ZERO)
        )


        val createdBy = ORCHESTRATOR.name
        val expectedGiveAwayExternalLines = listOf(
                GiveAwayExternalLine(
                        extLineId = extLineIdOne,
                        itemCode = "1",
                        unitAmountIncludingVat = 100.toBigDecimal(),
                        quantity = ONE
                )
        )
        val expectedGiveAway = giveAway(expectedGiveAwayExternalLines).copy(createdBy = createdBy)

        val expectedPaymentTask = paymentTaskWithoutDiscounts(paymentTaskLines)
        val taskId = expectedPaymentTask.taskId

        // when
        val actualGiveAwayId = paymentTaskService.createGiveAway(expectedPaymentTask)

        // then
        assertNotNull(actualGiveAwayId)

        verify(paymentTaskClient).addGiveAway(refEq(expectedGiveAway, "giveAwayId", "created") ?: expectedGiveAway, eq(taskId))
    }

    @Test
    fun `createGiveAway should create giveaway with external lines for specified lines and send request`() {
        val expectedPaymentTask = paymentTaskWithoutDiscounts(paymentTaskLines())
        val taskId = expectedPaymentTask.taskId

        val specifiedExternalLines = expectedPaymentTask.lines.map { GiveAwayLine(extLineId = it.extLineId!!, quantity = it.quantity) }
        val expectedGiveAwayExternalLines = expectedPaymentTask.lines
                .map {
                    GiveAwayExternalLine(
                            extLineId = it.extLineId!!,
                            itemCode = it.itemCode,
                            unitAmountIncludingVat = it.unitAmountIncludingVat,
                            quantity = it.quantity
                    )
                }
        val expectedGiveAway = giveAway(expectedGiveAwayExternalLines)

        paymentTaskService.createGiveAway(expectedPaymentTask, expectedGiveAway.createdBy, specifiedExternalLines)

        verify(paymentTaskClient).addGiveAway(check { assertThat(it).isEqualToIgnoringGivenFields(expectedGiveAway, "giveAwayId", "created") }, eq(taskId))
    }

    @Test
    fun `createGiveAway should create giveAway with external lines for whole paymentTaskLines only when depositQuantity is more than ZERO and send request`() {
        val extLineIdOne = "extLineIdOne"
        val extLineIdTwo = "extLineIdTwo"

        val paymentTaskLines = listOf(
                PaymentTaskLine(
                        extLineId = "extLineIdOne",
                        itemCode = "1",
                        lineStatus = "NEW",
                        lineType = "DELIVERY",
                        quantity = 4.toBigDecimal(),
                        unitAmountIncludingVat = 100.toBigDecimal(),
                        confirmedQuantity = 2.toBigDecimal(),
                        depositQuantity = 2.toBigDecimal()),
                PaymentTaskLine(
                        extLineId = extLineIdTwo,
                        itemCode = "2",
                        lineStatus = "NEW",
                        lineType = "DELIVERY",
                        quantity = 2.toBigDecimal(),
                        unitAmountIncludingVat = 200.toBigDecimal(),
                        confirmedQuantity = ONE,
                        depositQuantity = ZERO)
        )

        val createdBy = ORCHESTRATOR.name
        val expectedGiveAwayExternalLines = listOf(
                GiveAwayExternalLine(
                        extLineId = extLineIdOne,
                        itemCode = "1",
                        unitAmountIncludingVat = 100.toBigDecimal(),
                        quantity = 2.toBigDecimal()
                )
        )
        val expectedGiveAway = giveAway().copy(createdBy = createdBy, lines = expectedGiveAwayExternalLines)
        val expectedPaymentTask = paymentTaskWithoutDiscounts(paymentTaskLines)

        paymentTaskService.createGiveAway(expectedPaymentTask)

        verify(paymentTaskClient).addGiveAway(check { assertThat(it).isEqualToIgnoringGivenFields(expectedGiveAway, "giveAwayId", "created") }, eq(expectedPaymentTask.taskId))
    }

    @Test
    fun `startGiveAway should call camunda process to start with expected variables`() {
        // given
        val giveAwayId = "giveAwayId"
        val taskId = "taskId"
        val extOrderId = "extOrderId"
        val executionStore = 12

        val expectedPaymentTask = PaymentTask(
                taskId = taskId,
                taskStatus = "HOLD",
                taskType = "SOLUTION",
                extOrderId = extOrderId,
                executionStore = executionStore,
                lines = emptyList())

        // when
        paymentTaskService.startGiveAwayProcess(giveAwayId, expectedPaymentTask)

        // then
        verify(camundaService).startProcess(eq(Process.PAYMENT_GIVEAWAY), eq(giveAwayId), eq(mapOf(
                "paymentTaskId" to taskId,
                "extOrderId" to extOrderId,
                "executionStore" to executionStore
        )))
    }

    @Test
    fun `getGiveAways - ok`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val expected = listOf(giveAway(), giveAway())
        whenever(paymentTaskClient.getGiveAways(paymentTaskId)).thenReturn(expected)

        //When
        val actual = paymentTaskService.getGiveAways(paymentTaskId)

        //Then
        assertThat(actual).isEqualTo(expected)
    }

    private fun refundContext(): RefundContext =
            RefundContext(
                    extOrderId = "extOrderId",
                    paymentTaskId = "paymentTaskId",
                    currentPaymentStatus = "currentPaymentStatus",
                    lines = listOf(
                            RefundLine(
                                    extLineId = "extLineId",
                                    quantity = TEN,
                                    unitAmountIncludingVat = ONE
                            )
                    )
            )

    private fun giveAway(externalLines: Collection<GiveAwayExternalLine>? = null): GiveAway =
            GiveAway(
                    createdBy = "createdBy",
                    created = LocalDateTime.now(),
                    lines = externalLines ?: listOf(giveAwayExternalLine())
            )

    private fun giveAwayExternalLine(): GiveAwayExternalLine =
            GiveAwayExternalLine(
                    extLineId = "extLineId",
                    itemCode = "itemCode",
                    unitAmountIncludingVat = ONE,
                    quantity = TEN
            )

    private fun paymentTaskWithoutDiscounts(paymentTaskLines: Collection<PaymentTaskLine>? = null): PaymentTask =
            PaymentTask(
                    taskId = "taskId",
                    taskStatus = "HOLD",
                    taskType = "SOLUTION",
                    extOrderId = "extOrderId",
                    executionStore = 12,
                    lines = paymentTaskLines ?: paymentTaskLines())

    private fun paymentTaskLines(): Collection<PaymentTaskLine> =
            listOf(
                    PaymentTaskLine(
                            extLineId = "extLineIdOne",
                            itemCode = "1",
                            lineStatus = "NEW",
                            lineType = "DELIVERY",
                            quantity = 4.toBigDecimal(),
                            unitAmountIncludingVat = 100.toBigDecimal(),
                            confirmedQuantity = 2.toBigDecimal(),
                            depositQuantity = 2.toBigDecimal()),
                    PaymentTaskLine(
                            extLineId = "extLineIdTwo",
                            itemCode = "2",
                            lineStatus = "NEW",
                            lineType = "DELIVERY",
                            quantity = 2.toBigDecimal(),
                            unitAmountIncludingVat = 200.toBigDecimal(),
                            confirmedQuantity = ONE,
                            depositQuantity = ONE)
            )
}
