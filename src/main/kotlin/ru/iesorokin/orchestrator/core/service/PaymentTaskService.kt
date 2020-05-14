package ru.iesorokin.payment.orchestrator.core.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.FULL_APPROVE_KEY
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayLine
import ru.iesorokin.payment.orchestrator.core.domain.LineType
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskStatus.CONFIRMED
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskStatus.HOLD
import ru.iesorokin.payment.orchestrator.core.domain.RefundContext
import ru.iesorokin.payment.orchestrator.core.enums.Application.ORCHESTRATOR
import ru.iesorokin.payment.orchestrator.core.enums.ErrorCode
import ru.iesorokin.payment.orchestrator.core.enums.PaymentLineDiscountType
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.POD_AGENT
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.POD_POST_PAYMENT
import ru.iesorokin.payment.orchestrator.core.enums.TaskType.SBERLINK_WITH_TPNET_DEPOSIT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.exception.InvalidLineTypeException
import ru.iesorokin.payment.orchestrator.core.exception.InvalidTaskStatusException
import ru.iesorokin.payment.orchestrator.core.exception.InvalidUnitAmountIncludingVatException
import ru.iesorokin.payment.orchestrator.core.exception.LineNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.payment.task.PaymentTaskClient
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.LocalDateTime
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class PaymentTaskService(private val paymentTaskClient: PaymentTaskClient,
                         private val camundaService: CamundaService) {

    fun updateWorkflowId(paymentTaskId: String, workflowId: String) {
        log.info { "Update paymentTask $paymentTaskId with workflowId $workflowId" }
        paymentTaskClient.updateWorkflowId(paymentTaskId, workflowId)
    }

    fun updateTaskStatus(paymentTaskId: String, taskStatus: PaymentTransactionStatus) {
        log.info { "Update paymentTask $paymentTaskId with taskStatus $taskStatus" }
        paymentTaskClient.updateTaskStatus(paymentTaskId, taskStatus)
    }

    fun getPaymentTask(taskId: String) = paymentTaskClient.getPaymentTask(taskId)

    fun updateRegisterStatus(paymentTaskId: String, registerStatus: PaymentTaskRegisterStatus) {
        log.info { "Update paymentTask $paymentTaskId with registerSale  $registerStatus" }
        paymentTaskClient.updateRegisterStatus(paymentTaskId, registerStatus)
    }

    fun updateRefundStatusList(paymentTaskId: String, fiscalData: Collection<PaymentTaskFiscalData>) {
        log.info { "Update paymentTask $paymentTaskId with fiscalData  $fiscalData" }
        paymentTaskClient.updateRefundStatusList(paymentTaskId, fiscalData)
    }

    fun saveRefundLines(processInstanceId: String, refundContext: RefundContext) {
        paymentTaskClient.saveRefundLines(processInstanceId, refundContext)
    }

    fun editLines(taskId: String, updateBy: String, linesForUpdate: Collection<EditLine>) {
        validateVatOrQuantityNotNull(linesForUpdate)

        val paymentTask = paymentTaskClient.getPaymentTask(taskId)

        val extLineIdToPaymentTaskLine = paymentTask.lines.filter { it.extLineId != null }.associateBy { it.extLineId!! }

        validateExistLine(linesForUpdate.map { it.extLineId }, extLineIdToPaymentTaskLine)
        validateAllowedTaskTypeWithCorrectStatus(linesForUpdate, paymentTask)
        validateDeliveryStatus(linesForUpdate, extLineIdToPaymentTaskLine)
        validateAmountIncludingVat(linesForUpdate, extLineIdToPaymentTaskLine)

        paymentTaskClient.updateLines(taskId, updateBy, linesForUpdate)

        if (paymentTask.taskType == SBERLINK_WITH_TPNET_DEPOSIT.name) {
            if (paymentTask.workflowId == null) {
                throw IllegalStateException("Payment Task: $paymentTask doesn't have workflowId")
            }
            camundaService.updateVariableByProcessInstanceId(paymentTask.workflowId, FULL_APPROVE_KEY, false)
        }
    }

    private fun validateAllowedTaskTypeWithCorrectStatus(linesForUpdate: Collection<EditLine>, paymentTask: PaymentTask) {
        if (!isValidTaskStatus(paymentTask.taskType, paymentTask.taskStatus)) {
            throw InvalidTaskStatusException(ErrorCode.TASK_STATUS_ERROR.errorMessage + " EditLines: $linesForUpdate." +
                    "Actual payment task type ${paymentTask.taskType} with status ${paymentTask.taskStatus}")
        }
    }

    private fun isValidTaskStatus(taskType: String, taskStatus: String) =
            when (taskType) {
                SBERLINK_WITH_TPNET_DEPOSIT.name -> taskStatus == HOLD.name
                POD_POST_PAYMENT.name, POD_AGENT.name -> taskStatus == CONFIRMED.name
                else -> false
            }

    private fun validateVatOrQuantityNotNull(lines: Collection<EditLine>) {
        val linesWithEmptyVatAndQuantity = lines
                .filter { it.unitAmountIncludingVat == null && it.confirmedQuantity == null }
        if (linesWithEmptyVatAndQuantity.isNotEmpty()) {
            throw IllegalArgumentException("unitAmountIncludingVat or confirmedQuantity fields are required. Wrong lines: $linesWithEmptyVatAndQuantity")
        }
    }

    private fun validateExistLine(giveAwayLines: Collection<String>, taskLineByExtLineId: Map<String, PaymentTaskLine>) {
        val notValidLines = giveAwayLines.filterNot { taskLineByExtLineId.containsKey(it) }
        if (notValidLines.isNotEmpty()) {
            throw LineNotFoundException(ErrorCode.LINE_NOT_EXIST.errorMessage + " Not founded lines: " +
                    "$notValidLines. Available task lines: $taskLineByExtLineId")
        }
    }

    private fun validateDeliveryStatus(lines: Collection<EditLine>, map: Map<String, PaymentTaskLine>) {
        val notValidLines = lines.filterNot { map[it.extLineId]?.lineType == LineType.DELIVERY.name }
        if (notValidLines.isNotEmpty()) {
            throw InvalidLineTypeException(ErrorCode.LINE_TYPE_ERROR.errorMessage + " Lines with wrong delivery: " +
                    "$notValidLines. All payment lines: $map")
        }
    }

    private fun validateAmountIncludingVat(lines: Collection<EditLine>, map: Map<String, PaymentTaskLine>) {
        val notValidLines = lines
                .filterNot {
                    it.unitAmountIncludingVat!! >= ZERO &&
                            it.unitAmountIncludingVat <= map[it.extLineId]?.unitAmountIncludingVat
                }
        if (notValidLines.isNotEmpty()) {
            throw InvalidUnitAmountIncludingVatException(ErrorCode.VAT_ERROR.errorMessage +
                    " Lines with wrong unitAmountIncludingVat $notValidLines. All payment lines: $map")
        }
    }

    /**
     * Creates giveaway and sends request to paymentTask to store(add) one in database
     * While creating giveaway there are some rules, to create giveAwayExternalLine:
     *
     * 1. Create externalLines for whole payment task lines -> specifiedGiveAwayLine would be null
     * 2. Create externalLines from specified giveaway lines -> specifiedGiveAwayLine not null
     *
     * Also, if no createdBy was send, assuming ORCHESTRATOR creating giveAway
     */
    fun createGiveAway(paymentTask: PaymentTask,
                       createdBy: String = ORCHESTRATOR.name,
                       specifiedGiveAwayLine: Collection<GiveAwayLine>? = null): String {
        val giveAwayExternalLines = specifiedGiveAwayLine
                ?.let { validateAndMapToExternalLines(paymentTask, it) }
                ?: getExternalLinesForWholePaymentTaskLines(paymentTask)

        val giveAway = GiveAway(
                giveAwayId = UUID.randomUUID().toString(),
                createdBy = createdBy,
                created = LocalDateTime.now(),
                lines = giveAwayExternalLines)

        paymentTaskClient.addGiveAway(giveAway, paymentTask.taskId)

        return giveAway.giveAwayId!!
    }

    private fun getExternalLinesForWholePaymentTaskLines(paymentTask: PaymentTask) =
            paymentTask.lines
                    .filter { it.depositQuantity > ZERO }
                    .filter { it.extLineId != null }
                    .map {
                        GiveAwayExternalLine(
                                extLineId = it.extLineId!!,
                                itemCode = it.itemCode,
                                unitAmountIncludingVat = it.getGiveAwayUnitAmountIncludingVat(),
                                quantity = it.depositQuantity,
                                agencyAgreement = paymentTask.extLineIdToAgencyAgreement?.get(it.extLineId))
                    }

    private fun validateAndMapToExternalLines(paymentTask: PaymentTask,
                                              fromLines: Collection<GiveAwayLine>): Collection<GiveAwayExternalLine> {
        val extLineIdToPaymentTaskLine = paymentTask.lines
                .filter { it.extLineId != null }
                .associateBy { it.extLineId!! }
        validateExistLine(fromLines.map { it.extLineId }, extLineIdToPaymentTaskLine)

        return fromLines.mapToGiveAwayExternalLines(extLineIdToPaymentTaskLine, paymentTask)
    }

    fun startGiveAwayProcess(giveAwayId: String, paymentTask: PaymentTask) {
        camundaService.startProcess(Process.PAYMENT_GIVEAWAY, giveAwayId, mapOf(
                PAYMENT_TASK_ID to paymentTask.taskId,
                EXT_ORDER_ID to checkNotNull(paymentTask.extOrderId),
                EXECUTION_STORE to checkNotNull(paymentTask.executionStore)
        ))
    }

    fun getGiveAways(paymentTaskId: String): Collection<GiveAway> = paymentTaskClient.getGiveAways(paymentTaskId)

    private fun Collection<GiveAwayLine>.mapToGiveAwayExternalLines(extLineIdToPaymentTaskLineMap: Map<String, PaymentTaskLine>,
                                                                    paymentTask: PaymentTask): List<GiveAwayExternalLine> =
            this.map {
                // todo:
                // we duplicate this code, represented in validateExistLines, until refactoring, when
                // validation goes to tasks microservice
                val giveAwayLine = extLineIdToPaymentTaskLineMap[it.extLineId]
                        ?: throw LineNotFoundException("${ErrorCode.LINE_NOT_EXIST.errorMessage}. Not founded line $it")

                GiveAwayExternalLine(
                        extLineId = it.extLineId,
                        itemCode = giveAwayLine.itemCode,
                        unitAmountIncludingVat = giveAwayLine.getGiveAwayUnitAmountIncludingVat(),
                        quantity = it.quantity,
                        agencyAgreement = paymentTask.extLineIdToAgencyAgreement?.get(it.extLineId))
            }

    private fun PaymentTaskLine.getGiveAwayUnitAmountIncludingVat(): BigDecimal {
        return if (this.discount?.type == PaymentLineDiscountType.NEW_PRICE.name) {
            discount.typeValue
        } else {
            unitAmountIncludingVat
        }
    }

}
