package ru.iesorokin.orchestrator.core.service.giveaway

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.GIVE_AWAY
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK
import ru.iesorokin.orchestrator.input.stream.receiver.dto.AtolTransactionFiscalData
import ru.iesorokin.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolGiveAwayMessageServiceTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var atolGiveAwayMessageService: AtolGiveAwayMessageService

    @Test
    fun `processMessage should correlate message with processInstanceId`() {
        val process = startProcess()
        assertThat(process).isWaitingAt(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
        val message = AtolTransactionMessage(
                atolId = "atolId", status = "status", processInstanceId = process.processInstanceId,
                fiscalData = AtolTransactionFiscalData(
                        uuid = "uuid", ecrRegistrationNumber = "ecrRegistrationNumber",
                        fiscalDocumentNumber = 1L, fiscalStorageNumber = "fiscalStorageNumber")
        )

        atolGiveAwayMessageService.processMessage(message)

        assertThat(process).hasPassed(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
    }

    @Test
    fun `processMessage should correlate message with giveAwayId`() {
        val process = startProcess()
        assertThat(process).isWaitingAt(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
        val message = AtolTransactionMessage(
                atolId = "atolId", status = "status", processId = "giveAwayId",
                fiscalData = AtolTransactionFiscalData(
                        uuid = "uuid", ecrRegistrationNumber = "ecrRegistrationNumber",
                        fiscalDocumentNumber = 1L, fiscalStorageNumber = "fiscalStorageNumber")
        )

        atolGiveAwayMessageService.processMessage(message)

        assertThat(process).hasPassed(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
    }

    @Test
    fun `processMessage should correlate message with correlationKey`() {
        val process = startProcess()
        assertThat(process).isWaitingAt(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
        val message = AtolTransactionMessage(
                atolId = "atolId", status = "status", correlationKey = "giveAwayId",
                fiscalData = AtolTransactionFiscalData(
                        uuid = "uuid", ecrRegistrationNumber = "ecrRegistrationNumber",
                        fiscalDocumentNumber = 1L, fiscalStorageNumber = "fiscalStorageNumber")
        )

        atolGiveAwayMessageService.processMessage(message)

        assertThat(process).hasPassed(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
    }

    private fun startProcess() = rule.runtimeService
            .createProcessInstanceByKey(GIVE_AWAY)
            .businessKey("giveAwayId")
            .startBeforeActivity(RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK.code)
            .execute()
}
