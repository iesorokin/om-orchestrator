package ru.iesorokin.payment.orchestrator.output.client.solution

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.zalando.logbook.Logbook
import ru.iesorokin.payment.SOLUTION_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.getFileAsString
import ru.iesorokin.payment.orchestrator.config.RestTemplateConfig
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.exception.SolutionNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.dto.SolutionResponse
import ru.iesorokin.payment.orchestrator.output.client.solution.converter.SolutionConverter
import kotlin.test.assertEquals

class SolutionClientTest {

    private lateinit var solutionClient: SolutionClient
    private val solutionConverter = mock<SolutionConverter>()
    private val urlSolutionById = "/v1/solution/{solutionId}"

    @Rule
    @JvmField
    var wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().portNumber())

    @Before
    fun setUp() {
        val urlGetSolutionById = "http://localhost:${wireMock.port()}$urlSolutionById"
        val restTemplate = RestTemplateConfig(Logbook.create(), 10, 10)
                .restTemplateSolution(RestTemplateBuilder(), 5000, 5000)

        solutionClient = SolutionClient(
                restTemplateSolution = restTemplate,
                solutionConverter = solutionConverter,
                urlSolutionById = urlGetSolutionById
        )
    }

    @Test
    fun `should return solution by id`() {
        val solutionId = "123"

        val taskResponse = getFileAsObject<SolutionResponse>("${SOLUTION_FILE_PATH}solution-response-200.json")

        WireMock.stubFor(WireMock.get(urlSolutionById.replace("{solutionId}", solutionId))
                .willReturn(WireMock.okJson(getFileAsString("${SOLUTION_FILE_PATH}solution-response-200.json"))))

        val expected = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

        whenever(solutionConverter.convertSolutionResponseToSolution(taskResponse)).thenReturn(expected)

        val actual = solutionClient.getSolutionOrder(solutionId)
        assertEquals(expected, actual)
    }

    @Test(expected = SolutionNotFoundException::class)
    fun `should throw SolutionNotFoundException if response is null`() {
        val solutionId = "123"

        WireMock.stubFor(WireMock.get(urlSolutionById.replace("{solutionId}", solutionId))
                .willReturn(WireMock.okJson(null)))

        solutionClient.getSolutionOrder(solutionId)
    }
}
