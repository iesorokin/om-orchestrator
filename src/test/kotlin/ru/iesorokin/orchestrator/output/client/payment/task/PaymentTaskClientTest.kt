package ru.iesorokin.payment.orchestrator.output.client.payment.task

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.zalando.logbook.Logbook
import ru.iesorokin.payment.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.getFileAsString
import ru.iesorokin.payment.getObjectMapper
import ru.iesorokin.payment.orchestrator.config.RestTemplateConfig
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.payment.orchestrator.core.domain.RefundContext
import ru.iesorokin.payment.orchestrator.core.domain.RefundLine
import ru.iesorokin.payment.orchestrator.core.exception.PaymentTaskNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponse
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.GiveAwayConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.PaymentTaskConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.RefundDtosConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.AddGiveAwayLineRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchLineRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchLinesRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class PaymentTaskClientTest {
    private lateinit var paymentTaskClient: PaymentTaskClient
    private val paymentTaskConverter = mock<PaymentTaskConverter>()
    private val giveAwayConverter = spy<GiveAwayConverter>()
    private val refundConverter = RefundDtosConverter()
    private val endpointPatch = "/v1/task/{taskId}"
    private val endpointGetTask = "/v1/task/{taskId}"
    private val endpointPutStatus = "/v1/task/chanage-status/{taskId}"
    private val endpointPathLine = "/orchestrator/{taskId}/edit-line"
    private val urlPostGiveAway = "/v1/task/{paymentTaskId}/give-away"
    private val urlGetGiveAways = "/v1/task/{paymentTaskId}/give-away"

    @Rule
    @JvmField
    var wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().portNumber())

    @Before
    fun setUp() {
        val urlPatch = "http://localhost:${wireMock.port()}$endpointPatch"
        val urlGetTask = "http://localhost:${wireMock.port()}$endpointGetTask"
        val urlStatusPut = "http://localhost:${wireMock.port()}$endpointPutStatus"
        val urlPathLine = "http://localhost:${wireMock.port()}$endpointPathLine"
        val urlPostGiveAway = "http://localhost:${wireMock.port()}$urlPostGiveAway"
        val urlGetGiveAways = "http://localhost:${wireMock.port()}$urlGetGiveAways"
        val restTemplate = RestTemplateConfig(Logbook.create(), 10, 10)
                .restTemplatePaymentTask(RestTemplateBuilder(), 5000, 5000)
        paymentTaskClient = PaymentTaskClient(
                restTemplatePaymentTask = restTemplate,
                paymentTaskConverter = paymentTaskConverter,
                giveAwayConverter = giveAwayConverter,
                refundConverter = refundConverter,
                urlPatch = urlPatch,
                urlGetPaymentTask = urlGetTask,
                urlStatusPut = urlStatusPut,
                urlPathLine = urlPathLine,
                urlPostGiveAway = urlPostGiveAway,
                urlGetGiveAways = urlGetGiveAways
        )
    }

    @Test
    fun `updateWorkflowId should invoke patch url with jsonPatch body and contentType`() {
        val taskId = "12345678"
        val workflowId = "newWorkflowId"

        stubFor(patch(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        { "op": "replace", "path": "/workflowId", "value": "$workflowId" }
                    ]
                """.trimIndent()))
                .willReturn(okJson(getFileAsString("${PAYMENT_TASK_FILE_PATH}task-response-200.json"))))

        paymentTaskClient.updateWorkflowId(taskId, workflowId)

        verify(patchRequestedFor(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        { "op": "replace", "path": "/workflowId", "value": "$workflowId" }
                    ]
                """.trimIndent()))
                .withHeader("Content-Type", equalTo("application/json-patch+json")))
    }

    @Test
    fun `getPaymentTask should return payment task`() {
        val taskId = "12345678"

        val taskResponse = getFileAsObject<PaymentTaskResponse>("${PAYMENT_TASK_FILE_PATH}task-response-200.json")

        stubFor(get(endpointGetTask.replace("{taskId}", taskId))
                .willReturn(okJson(getFileAsString("${PAYMENT_TASK_FILE_PATH}task-response-200.json"))))

        val expected = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")

        whenever(paymentTaskConverter.convertPaymentTaskResponseToPaymentTask(taskResponse)).thenReturn(expected)

        val actual = paymentTaskClient.getPaymentTask(taskId)
        assertEquals(expected, actual)
    }

    @Test(expected = PaymentTaskNotFoundException::class)
    fun `getPaymentTask should throw exception if response is null`() {
        val taskId = "12345678"

        stubFor(get(endpointGetTask.replace("{taskId}", taskId))
                .willReturn(okJson(null)))

        paymentTaskClient.getPaymentTask(taskId)
    }

    @Test
    fun `updateRegisterStatus should invoke patch url with jsonPatch body with non null fields and contentType`() {
        val testFunction = { registerStatus: PaymentTaskRegisterStatus, requestJson: String ->
            val taskId = "12345678"

            stubFor(patch(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                    .withRequestBody(equalToJson(requestJson, true, false))
                    .willReturn(okJson(getFileAsString("${PAYMENT_TASK_FILE_PATH}task-response-200-with-register-status.json"))))

            paymentTaskClient.updateRegisterStatus(taskId, registerStatus)

            verify(patchRequestedFor(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                    .withRequestBody(equalToJson(requestJson, true, false))
                    .withHeader("Content-Type", equalTo("application/json-patch+json")))
        }
        testFunction.invoke(
                PaymentTaskRegisterStatus(
                        lines = listOf(
                                PaymentTaskRegisterStatusLine("1", "10".toBigDecimal(), "100".toBigDecimal()),
                                PaymentTaskRegisterStatusLine("2", "220".toBigDecimal(), "952".toBigDecimal()),
                                PaymentTaskRegisterStatusLine("3", "801".toBigDecimal(), "7594".toBigDecimal())
                        )
                ),
                """[
                        {
                            "op": "add",
                            "path": "/registerStatus/lines",
                            "value": [
                                        {
                                            "extLineId": "1",
                                            "quantity": 10,
                                            "unitAmountIncludingVat": 100
                                        },
                                            {
                                            "extLineId": "2",
                                            "quantity": 220,
                                            "unitAmountIncludingVat": 952
                                        },
                                            {
                                            "extLineId": "3",
                                            "quantity": 801,
                                            "unitAmountIncludingVat": 7594
                                        }
                                    ]
                        }
                    ]
                """.trimIndent()
        )
        testFunction.invoke(
                PaymentTaskRegisterStatus(
                        atolId = "atolId",
                        uuid = "4412-4123-551"
                ),
                """[
                        {
                            "op": "add",
                            "path": "/registerStatus/uuid",
                            "value": "4412-4123-551"
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/atolId",
                            "value": "atolId"
                        }
                    ]
                """.trimIndent()
        )
        testFunction.invoke(
                PaymentTaskRegisterStatus(
                        atolId = "atolId",
                        uuid = "4412-4123-551",
                        ecrRegistrationNumber = "ecrRegistrationNumberValue",
                        fiscalDocumentNumber = 421,
                        fiscalStorageNumber = "fiscalStorageNumber",
                        status = "DONE"
                ),
                """[
                        {
                            "op": "add",
                            "path": "/registerStatus/atolId",
                            "value": "atolId"
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/uuid",
                            "value": "4412-4123-551"
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/ecrRegistrationNumber",
                            "value": "ecrRegistrationNumberValue"
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/fiscalDocumentNumber",
                            "value": 421
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/fiscalStorageNumber",
                            "value": "fiscalStorageNumber"
                        },
                        {
                            "op": "add",
                            "path": "/registerStatus/status",
                            "value": "DONE"
                        }
                    ]
                """.trimIndent()
        )
    }

    @Test
    fun `should save refund lines`() {
        //Given
        val taskId = "12345678"
        val processInstanceId = "processInstanceId"
        stubFor(patch(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        {
                            "op": "add",
                            "path": "/refundStatusList/-",
                            "value": {
                                "status": "REFUND",
                                "refundWorkflowId": "processInstanceId",
                                "lines": [
                                     {
                                        "extLineId": "67a7148a-b01a-4935-8a9e-f5ad05f79c61",
                                        "quantity": 10,
                                        "unitAmountIncludingVat": 1 
                                     }
                                ]
                            }
                        }
                    ]
                """.trimIndent())))

        //When
        paymentTaskClient.saveRefundLines(processInstanceId, refundContext())

        //Then
        verify(patchRequestedFor(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        {
                            "op": "add",
                            "path": "/refundStatusList/-",
                            "value": {
                                "status": "REFUND",
                                "refundWorkflowId": "processInstanceId",
                                "lines": [
                                    {
                                        "extLineId": "67a7148a-b01a-4935-8a9e-f5ad05f79c61",
                                        "quantity": 10,
                                        "unitAmountIncludingVat": 1
                                    }
                                ]
                            }
                        }
                    ]
                """.trimIndent()))
                .withHeader("Content-Type", equalTo("application/json-patch+json")))
    }

    @Test
    fun `should add give away`() {
        val createdBy = "createdBy"
        val paymentTaskId = "paymentTaskId"
        val giveAwayId = UUID.randomUUID().toString()

        val lines = listOf(
                GiveAwayExternalLine(
                        extLineId = "extLineId1",
                        itemCode = "itemCode1",
                        unitAmountIncludingVat = BigDecimal.TEN,
                        quantity = 11.toBigDecimal()
                ),
                GiveAwayExternalLine(
                        extLineId = "extLineId2",
                        itemCode = "itemCode2",
                        unitAmountIncludingVat = 20.toBigDecimal(),
                        quantity = 21.toBigDecimal()
                )
        )

        val giveAwayLinesRequest = listOf(
                AddGiveAwayLineRequest(
                        extLineId = "extLineId1",
                        itemCode = "itemCode1",
                        unitAmountIncludingVat = BigDecimal.TEN,
                        quantity = 11.toBigDecimal()
                ),
                AddGiveAwayLineRequest(
                        extLineId = "extLineId2",
                        itemCode = "itemCode2",
                        unitAmountIncludingVat = 20.toBigDecimal(),
                        quantity = 21.toBigDecimal()
                )
        )

        @Language("JSON")
        val giveAwayRequest = """
            {
              "createdBy": "$createdBy",
              "giveAwayId": "$giveAwayId",
              "lines": [
                {
                  "extLineId": "extLineId1",
                  "itemCode": "itemCode1",
                  "unitAmountIncludingVat": 10,
                  "quantity": 11
                },
                {
                  "extLineId": "extLineId2",
                  "itemCode": "itemCode2",
                  "unitAmountIncludingVat": 20,
                  "quantity": 21
                }
              ]
            }
        """.trimIndent()

        whenever(giveAwayConverter.toGiveAwayLinesRequest(lines)).thenReturn(giveAwayLinesRequest)

        stubFor(post(urlPostGiveAway.replace("{paymentTaskId}", paymentTaskId))
                .withRequestBody(equalToJson(giveAwayRequest))
                .willReturn(WireMock.ok()))

        val giveAway = GiveAway(giveAwayId = giveAwayId, createdBy = createdBy, lines = lines, created = LocalDateTime.now())

        paymentTaskClient.addGiveAway(giveAway, paymentTaskId)

        verify(postRequestedFor(urlEqualTo(urlPostGiveAway.replace("{paymentTaskId}", paymentTaskId)))
                .withRequestBody(equalToJson(giveAwayRequest)))
    }

    @Test
    fun `updateRefundStatusList should invoke patch url with jsonPatch body and contentType`() {
        val taskId = "12345678"
        val registerStatus: Collection<PaymentTaskFiscalData> = getObjectMapper().readValue(refundStatusListJson)

        stubFor(patch(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        { "op": "replace", "path": "/refundStatusList", "value": $refundStatusListJson }
                    ]
                """.trimIndent()))
                .willReturn(okJson(getFileAsString("${PAYMENT_TASK_FILE_PATH}task-response-200-with-register-status.json"))))

        paymentTaskClient.updateRefundStatusList(taskId, registerStatus)

        verify(patchRequestedFor(urlEqualTo(endpointPatch.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson("""
                    [
                        { "op": "replace", "path": "/refundStatusList", "value": $refundStatusListJson }
                    ]
                """.trimIndent()))
                .withHeader("Content-Type", equalTo("application/json-patch+json")))
    }

    @Test
    fun `updateLines should invoke patch url with jsonPatch body and contentType`() {
        val taskId = "12345678"

        val pathLine = PatchLinesRequest(
                updateBy = "system",
                lines = listOf(
                        PatchLineRequest(
                                extLineId = "extLineId1",
                                unitAmountIncludingVat = BigDecimal.ONE,
                                confirmedQuantity = BigDecimal.TEN
                        )
                )
        )

        val lines = listOf(
                EditLine(
                        extLineId = "extLineId1",
                        unitAmountIncludingVat = BigDecimal.ONE,
                        confirmedQuantity = BigDecimal.TEN
                )
        )
        stubFor(patch(urlEqualTo(endpointPathLine.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson(pathLine.toJson()))
                .willReturn(okJson(getFileAsString("${PAYMENT_TASK_FILE_PATH}task-response-200-with-register-status.json"))))

        whenever(paymentTaskConverter.convertEditLinesToPathLinesRequest(lines)).thenReturn(pathLine.lines.toList())

        paymentTaskClient.updateLines(taskId, "system", lines)

        verify(patchRequestedFor(urlEqualTo(endpointPathLine.replace("{taskId}", taskId)))
                .withRequestBody(equalToJson(pathLine.toJson()))
                .withHeader("Content-Type", equalTo("application/json-patch+json")))
    }

    @Test
    fun `getGiveAways - ok`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val expected: Collection<GiveAway> = listOf(
                GiveAway(
                        created = LocalDateTime.of(2019, 10, 10, 10, 10,
                                10, 100000000),
                        processInstanceId = "processInstanceId",
                        createdBy = "creator",
                        lines = emptyList()
                )
        )
        stubFor(
                get(urlGetGiveAways.replace("{paymentTaskId}", paymentTaskId))
                        .willReturn(okJson(giveAwayResponses))
        )

        //When
        val actual = paymentTaskClient.getGiveAways(paymentTaskId)

        //Then
        assertThat(actual).isEqualTo(expected)
    }

    private inline fun PatchLinesRequest.toJson() = Gson().toJson(this)

    private fun refundContext(): RefundContext =
            RefundContext(
                    extOrderId = "extOrderId",
                    paymentTaskId = "12345678",
                    currentPaymentStatus = "currentPaymentStatus",
                    lines = listOf(
                            RefundLine(
                                    extLineId = "67a7148a-b01a-4935-8a9e-f5ad05f79c61",
                                    quantity = BigDecimal.TEN,
                                    unitAmountIncludingVat = BigDecimal.ONE
                            )
                    )
            )

    @Language("JSON")
    val registerStatusJson = """
        {
          "atolId": "atolId1",
          "uuid": "4412-4123-551",
          "ecrRegistrationNumber": "ecrRegistrationNumber1",
          "fiscalDocumentNumber": 3221,
          "fiscalStorageNumber": "fiscalStorageNumber1",
          "status": "DONE",
          "lines": null
        }"""

    @Language("JSON")
    val refundStatusListJson = """
        [
          {
            "created" : null,
            "atolId" : "atolId1",
            "status" : null,
            "uuid" : "uuid1",
            "ecrRegistrationNumber" : "ecrRegistrationNumber1",
            "fiscalDocumentNumber" : 21,
            "fiscalStorageNumber" : "fiscalStorageNumber1",
            "refundWorkflowId": "workflowId1",
            "lines": [
              {
                "extLineId": "extLineId1",
                "lineId": "1",
                "quantity": 100,
                "unitAmountIncludingVat": 110
              },
              {
                "extLineId": "extLineId2",
                "lineId": "2",
                "quantity": 200,
                "unitAmountIncludingVat": 120
              }
            ]
          }
  ]"""

    @Language("JSON")
    val giveAwayResponses = """
        [
          {
            "created" : "2019-10-10T10:10:10.100",
            "processInstanceId": "processInstanceId",
            "createdBy": "creator",
            "lines": []
          }
        ]
  """
}
