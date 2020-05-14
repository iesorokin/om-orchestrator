package ru.iesorokin.payment.orchestrator.output.client.solution

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.exception.SolutionNotFoundException
import ru.iesorokin.payment.orchestrator.output.client.dto.SolutionResponse
import ru.iesorokin.payment.orchestrator.output.client.solution.converter.SolutionConverter


private val log = KotlinLogging.logger {}

@Component
@RefreshScope
class SolutionClient(val restTemplateSolution: RestTemplate,
                     val solutionConverter: SolutionConverter,
                     @Value("\${solution.urlSolutionById}") private val urlSolutionById: String) {

    fun getSolutionOrder(solutionId: String): Solution {
        val response = restTemplateSolution
                .getForObject(urlSolutionById, SolutionResponse::class.java, solutionId)
                ?: throw SolutionNotFoundException("Solution not found with id $solutionId")
        return solutionConverter.convertSolutionResponseToSolution(response)
    }
}
