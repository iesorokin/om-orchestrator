package ru.iesorokin.payment.orchestrator.output.client.payment.task

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.RefundContext
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.exception.GiveAwaysNotFoundException
import ru.iesorokin.payment.orchestrator.core.exception.PaymentClientException
import ru.iesorokin.payment.orchestrator.core.exception.PaymentTaskNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.dto.JsonPatchRequestOperation
import ru.iesorokin.payment.orchestrator.output.client.dto.OperationType
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponse
import ru.iesorokin.payment.orchestrator.output.client.dto.UpdateTaskStatusRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.GiveAwayConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.PaymentTaskConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.RefundDtosConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.GiveAwayResponseItem
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchLinesRequest
import kotlin.reflect.full.memberProperties

private const val APPLICATION_JSON_PATCH = "application/json-patch+json"
private val log = KotlinLogging.logger {}


@Component
@RefreshScope
class PaymentTaskClient(private val restTemplatePaymentTask: RestTemplate,
                        private val paymentTaskConverter: PaymentTaskConverter,
                        private val refundConverter: RefundDtosConverter,
                        private val giveAwayConverter: GiveAwayConverter,
                        @Value("\${payment.task.urlPatch}")
                        private val urlPatch: String,
                        @Value("\${payment.task.urlGetPaymentTask}")
                        private val urlGetPaymentTask: String,
                        @Value("\${payment.task.urlGetGiveAways}")
                        private val urlGetGiveAways: String,
                        @Value("\${payment.task.urlStatusPut}")
                        private val urlStatusPut: String,
                        @Value("\${payment.task.urlPathLine}")
                        private val urlPathLine: String,
                        @Value("\${payment.task.urlPostGiveAway}")
                        private val urlPostGiveAway: String) {

    fun getPaymentTask(taskId: String): PaymentTask {
        log.info { "Get payment task with id $taskId" }
        val response = restTemplatePaymentTask.getForObject(urlGetPaymentTask,
                PaymentTaskResponse::class.java, taskId)
                ?: throw PaymentTaskNotFoundException("Payment task not found with id $taskId")
        return paymentTaskConverter.convertPaymentTaskResponseToPaymentTask(response)
    }

    fun updateTaskStatus(paymentTaskId: String, taskStatus: PaymentTransactionStatus) {
        val request = UpdateTaskStatusRequest("CAMUNDA", taskStatus)
        restTemplatePaymentTask.put(urlStatusPut, request, paymentTaskId)
    }

    fun updateWorkflowId(paymentTaskId: String, workflowId: String) {
        val body = listOf(JsonPatchRequestOperation(OperationType.REPLACE, "/workflowId", workflowId))
        doPatch(paymentTaskId, body)
    }

    fun updateRegisterStatus(paymentTaskId: String, registerStatus: PaymentTaskRegisterStatus) {
        val body = buildPatchRequest(registerStatus, "/registerStatus")
        doPatch(paymentTaskId, body)
    }

    private fun buildPatchRequest(obj: Any, objPath: String): Collection<JsonPatchRequestOperation> {
        val request = mutableListOf<JsonPatchRequestOperation>()
        for (property in obj.javaClass.kotlin.memberProperties) {
            val propertyValue = property.get(obj)
            if (propertyValue != null) {
                request.add(JsonPatchRequestOperation(OperationType.ADD, "$objPath/${property.name}", propertyValue))
            }
        }
        return request
    }

    fun updateRefundStatusList(paymentTaskId: String, fiscalData: Collection<PaymentTaskFiscalData>) {
        val body = listOf(JsonPatchRequestOperation(OperationType.REPLACE, "/refundStatusList", fiscalData))
        doPatch(paymentTaskId, body)
    }

    fun saveRefundLines(processInstanceId: String, refundContext: RefundContext) {
        val body = refundConverter.toRequest(processInstanceId, refundContext)
        doPatch(refundContext.paymentTaskId, body)
    }

    private fun doPatch(paymentTaskId: String, body: Collection<JsonPatchRequestOperation>) {
        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_PATCH)
        }
        val request = HttpEntity(body, headers)
        restTemplatePaymentTask.patchForObject(urlPatch, request, Void::class.java, paymentTaskId)
    }

    fun updateLines(paymentTaskId: String, updateBy: String, lines: Collection<EditLine>) {
        val pathLinesRequest = paymentTaskConverter.convertEditLinesToPathLinesRequest(lines)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_PATCH)
        }
        val request = HttpEntity(PatchLinesRequest(updateBy = updateBy, lines = pathLinesRequest), headers)
        restTemplatePaymentTask.patchForObject(urlPathLine, request, Void::class.java, paymentTaskId)
    }

    fun addGiveAway(giveAway: GiveAway, paymentTaskId: String) {
        try {
            val headers = HttpHeaders().apply {
                add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_PATCH)
            }

            val giveAwayRequest = giveAwayConverter.toAddGiveAwayRequest(giveAway)

            val request = HttpEntity(giveAwayRequest, headers)
            restTemplatePaymentTask.postForObject(urlPostGiveAway, request, Void::class.java, paymentTaskId)
        } catch (ex: Exception) {
            throw PaymentClientException("An error occurred during add-give-away-request. createdBy: ${giveAway.createdBy}, " +
                    "businessKey: ${giveAway.giveAwayId}, paymentTaskId: $paymentTaskId, lines: ${giveAway.lines}")
        }
    }

    fun getGiveAways(paymentTaskId: String): Collection<GiveAway> {
        log.info { "Get giveaways for payment task id: $paymentTaskId" }
        val giveAwayResponseItems: Collection<GiveAwayResponseItem> = restTemplatePaymentTask.exchange(
                urlGetGiveAways,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<Collection<GiveAwayResponseItem>>() {},
                paymentTaskId
        ).body
                ?: throw GiveAwaysNotFoundException("Some problem with rest to payment tasks. " +
                        "Response body was not returned for paymentTaskId: $paymentTaskId")

        return giveAwayConverter.toGiveAways(giveAwayResponseItems)
    }
}
