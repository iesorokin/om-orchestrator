package ru.iesorokin.payment.orchestrator.core.service

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.Mockito.times
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.payment.orchestrator.output.client.solution.SolutionClient
import ru.iesorokin.payment.orchestrator.output.stream.sender.FiscalizationStatusSender

class SolutionServiceTest {
    private val solutionClient = mock<SolutionClient>()
    private val fiscalizationStatusSender = mock<FiscalizationStatusSender>()
    private val solutionService = SolutionService(
            solutionClient = solutionClient,
            fiscalizationStatusSender = fiscalizationStatusSender
    )

    @Test
    fun `getSolutionOrder should return solution`() {
        val solutionId = "testSolutionId"
        val expected = Solution(
                solutionId = solutionId
        )
        whenever(solutionClient.getSolutionOrder(solutionId)).thenReturn(expected)

        val actual = solutionService.getSolutionOrder(solutionId)

        Assertions.assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun `sendFiscalizationStatus should call fiscalizationStatusSender`() {
        val paymentTaskId = "testPaymentTaskId"
        val solutionId = "testSolutionId"
        val status = FiscalizationStatus.FISCALIZATION_STARTED

        doNothing().`when`(fiscalizationStatusSender).sendFiscalizationStatus(paymentTaskId, solutionId, status)

        solutionService.sendFiscalizationStatus(paymentTaskId, solutionId, status)

        verify(fiscalizationStatusSender, times(1)).sendFiscalizationStatus(paymentTaskId, solutionId, status)
    }
}
