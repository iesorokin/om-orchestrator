package ru.iesorokin.orchestrator.output.client.payment.atol

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.zalando.logbook.Logbook
import ru.iesorokin.ATOL_FILE_PATH
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.SOLUTION_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.getFileAsString
import ru.iesorokin.orchestrator.config.RestTemplateConfig
import ru.iesorokin.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.exception.EmptyAtolResponseException
import ru.iesorokin.orchestrator.output.client.dto.AtolGiveAwayRegisterRequest
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequest
import ru.iesorokin.orchestrator.output.client.payment.atol.converter.AtolConverter
import kotlin.test.assertEquals

class AtolClientTest {
    private lateinit var atolClient: AtolClient
    private val atolConverter = mock<AtolConverter>()
    private val endpointRegisterSale = "/register-sale"
    private val endpointRegisterRefund = "/register-refund"
    private val endpointRegisterGiveAway = "/register-giveaway"

    private val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
    private val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
    private val atolGiveAway = getFileAsObject<AtolGiveAway>("${ATOL_FILE_PATH}atol-give-away.json")
    private val atolRegisterRequest = getFileAsObject<AtolRegisterRequest>("${ATOL_FILE_PATH}atol-request-success.json")

    @Rule
    @JvmField
    var wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().portNumber())

    @Before
    fun setUp() {
        val urlRegisterSale = "http://localhost:${wireMock.port()}$endpointRegisterSale"
        val urlRegisterRefund = "http://localhost:${wireMock.port()}$endpointRegisterRefund"
        val urlRegisterGiveAway = "http://localhost:${wireMock.port()}$endpointRegisterGiveAway"
        val restTemplate = RestTemplateConfig(Logbook.create(), 10, 10)
                .restTemplateAtol(RestTemplateBuilder(), 5000, 5000)
        atolClient = AtolClient(
                restTemplateAtol = restTemplate,
                atolConverter = atolConverter,
                urlRegisterSale = urlRegisterSale,
                urlRegisterRefund = urlRegisterRefund,
                urlRegisterGiveAway = urlRegisterGiveAway
        )
    }

    @Test
    fun `registerSale should return atolId`() {
        val atolId = "atolId"

        WireMock.stubFor(
                WireMock.post(endpointRegisterSale)
                        .withRequestBody(
                                equalToJson(
                                        getFileAsString("${ATOL_FILE_PATH}atol-request-success.json")
                                )
                        )
                        .willReturn(WireMock.okJson(getFileAsString("${ATOL_FILE_PATH}atol-response-200.json"))))

        whenever(atolConverter.convertTaskAndSolutionToSaleRequest(task, solution)).thenReturn(atolRegisterRequest)

        val actualId = atolClient.registerSale(task, solution)
        assertEquals(atolId, actualId)
    }

    @Test(expected = EmptyAtolResponseException::class)
    fun `registerSale should throw exception if response is null`() {
        WireMock.stubFor(
                WireMock.post(endpointRegisterSale)
                        .withRequestBody(
                                equalToJson(
                                        getFileAsString("${ATOL_FILE_PATH}atol-request-success.json")
                                )
                        )
                        .willReturn(WireMock.okJson(null)))

        whenever(atolConverter.convertTaskAndSolutionToSaleRequest(task, solution)).thenReturn(atolRegisterRequest)

        atolClient.registerSale(task, solution)
    }

    @Test
    fun `registerRefund should return atolId`() {
        val atolId = "atolId"
        val workflowId = "workflowId"

        WireMock.stubFor(
                WireMock.post(endpointRegisterRefund)
                        .withRequestBody(
                                equalToJson(
                                        getFileAsString("${ATOL_FILE_PATH}atol-request-success.json")
                                )
                        )
                        .willReturn(WireMock.okJson(getFileAsString("${ATOL_FILE_PATH}atol-response-200.json"))))

        whenever(atolConverter.convertTaskAndSolutionToRefundRequest(task, solution, workflowId)).thenReturn(atolRegisterRequest)

        val actualId = atolClient.registerRefund(task, solution, workflowId)
        assertEquals(atolId, actualId)
    }

    @Test(expected = EmptyAtolResponseException::class)
    fun `registerRefund should throw exception if response is null`() {
        val workflowId = "workflowId"

        WireMock.stubFor(
                WireMock.post(endpointRegisterRefund)
                        .withRequestBody(
                                equalToJson(
                                        getFileAsString("${ATOL_FILE_PATH}atol-request-success.json")
                                )
                        )
                        .willReturn(WireMock.okJson(null)))

        whenever(atolConverter.convertTaskAndSolutionToRefundRequest(task, solution, workflowId)).thenReturn(atolRegisterRequest)

        atolClient.registerRefund(task, solution, workflowId)
    }

    @Test
    fun `registerGiveAway should return atolId`() {
        val atolId = "atolId"
        val atolRegisterGiveAwayRequest =
                getFileAsObject<AtolGiveAwayRegisterRequest>("${ATOL_FILE_PATH}atol-request-success.json")
                .copy(processInstanceId = "processInstanceId", giveAwayId = "giveAwayId")

        WireMock.stubFor(
                WireMock.post(endpointRegisterGiveAway)
                        .withRequestBody(
                                equalToJson(
                                        getFileAsString("${ATOL_FILE_PATH}atol-request-success-with-process-instance-id.json")
                                )
                        )
                        .willReturn(WireMock.okJson(getFileAsString("${ATOL_FILE_PATH}atol-response-200.json"))))

        whenever(atolConverter.convertAtolGiveAwayToAtolGiveAwayRequest(atolGiveAway)).thenReturn(atolRegisterGiveAwayRequest)

        val actualId = atolClient.registerGiveAway(atolGiveAway)
        assertEquals(atolId, actualId)
    }
}
