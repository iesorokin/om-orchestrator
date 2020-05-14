package ru.iesorokin.orchestrator.output.client.payment.atol.converter

import org.assertj.core.api.Assertions
import org.junit.Test
import ru.iesorokin.ATOL_FILE_PATH
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.SOLUTION_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.core.domain.AtolGiveAway
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.exception.EmptyFieldException
import ru.iesorokin.orchestrator.output.client.dto.AtolGiveAwayRegisterRequest
import ru.iesorokin.orchestrator.output.client.dto.AtolRegisterRequest

class AtolConverterTest {

    private val atolConverter = AtolConverter()

    @Test
    fun `convertTaskAndSolutionToSaleRequest should convert dto to domain model`() {
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
        val expected = getFileAsObject<AtolRegisterRequest>("${ATOL_FILE_PATH}atol-request-without-agency-agreement.json")

        val actual = atolConverter.convertTaskAndSolutionToSaleRequest(task, solution)

        Assertions.assertThat(expected).isEqualToIgnoringGivenFields(actual, "dateTime")
    }

    @Test
    fun `convertTaskAndSolutionToSaleRequest should convert dto to domain model for pod_post_payment taskType`() {
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-pod-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
        val expected = getFileAsObject<AtolRegisterRequest>("${ATOL_FILE_PATH}atol-request-pod-task.json")

        val actual = atolConverter.convertTaskAndSolutionToSaleRequest(task, solution)

        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected, "dateTime")
    }

    @Test
    fun `convertTaskAndSolutionToRefundRequest should convert dto to domain model`() {
        val workflowId = "98765"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-with-refund-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
        val expected = getFileAsObject<AtolRegisterRequest>("${ATOL_FILE_PATH}atol-request-with-refund-success.json")

        val actual = atolConverter.convertTaskAndSolutionToRefundRequest(task, solution, workflowId)

        Assertions.assertThat(expected).isEqualToIgnoringGivenFields(actual, "dateTime")
    }

    @Test(expected = EmptyFieldException::class)
    fun `convertTaskAndSolutionToRefundRequest should fail dto to domain model`() {
        val workflowId = "98765"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-with-refund-fail.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")

        atolConverter.convertTaskAndSolutionToRefundRequest(task, solution, workflowId)
    }

    @Test
    fun `convertAtolGiveAwayToAtolGiveAwayRequest should convert AtolGiveAway to AtolGiveAwayRequest`() {
        val atolGiveAway = getFileAsObject<AtolGiveAway>("${ATOL_FILE_PATH}atol-give-away.json")
        val expected = getFileAsObject<AtolGiveAwayRegisterRequest>("${ATOL_FILE_PATH}atol-request-success-with-process-instance-id.json")

        val actual = atolConverter.convertAtolGiveAwayToAtolGiveAwayRequest(atolGiveAway)

        Assertions.assertThat(expected).isEqualToIgnoringGivenFields(actual)
    }
}
