package ru.iesorokin.orchestrator.output.client.solution.converter

import org.junit.Test
import ru.iesorokin.SOLUTION_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponse
import kotlin.test.assertEquals

class SolutionConverterTest {
    @Test
    fun `convertSolutionResponseToSolution should convert dto to domain model`() {
        val response = getFileAsObject<SolutionResponse>("${SOLUTION_FILE_PATH}solution-response-200.json")
        val expected = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

        val actual = SolutionConverter().convertSolutionResponseToSolution(response)

        assertEquals(expected, actual)
    }
}
