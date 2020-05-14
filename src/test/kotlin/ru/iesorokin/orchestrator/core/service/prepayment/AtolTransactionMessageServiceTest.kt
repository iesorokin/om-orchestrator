package ru.iesorokin.orchestrator.core.service.prepayment

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.ATOL_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.RECEIVE_ATOL_SUCCESS_RECEIVE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.orchestrator.core.task.common.SaveDataFromAtolTask
import ru.iesorokin.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolTransactionMessageServiceTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var atolTransactionMessageService: AtolTransactionMessageService
    @MockBean
    private lateinit var saveDataFromAtolTask: SaveDataFromAtolTask

    @get:Rule
    val thrown = ExpectedException.none()!!

    @Test
    fun `should successfully correlate received message and SBERBANK_PREPAYMENT_WITH_TPNET pass through the task`() {
        doNothing().`when`(saveDataFromAtolTask).execute(any())
        val atolMessage = getFileAsObject<AtolTransactionMessage>("${ATOL_FILE_PATH}atol-message-success.json")
        val context = mapOf(
                ATOL_REGISTER_ID to atolMessage.atolId
        )

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
                .businessKey("correlationKey")
                .setVariables(context)
                .execute()

        atolTransactionMessageService.processMessage(atolMessage)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `should successfully correlate received message and POD_PAYMENT pass through the task`() {
        doNothing().`when`(saveDataFromAtolTask).execute(any())
        val atolMessage = getFileAsObject<AtolTransactionMessage>("${ATOL_FILE_PATH}atol-message-success.json")
        val context = mapOf(
                ATOL_REGISTER_ID to atolMessage.atolId
        )

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .startBeforeActivity(PodProcessElement.RECEIVE_ATOL_SUCCESS.code)
                .businessKey("correlationKey")
                .setVariables(context)
                .execute()

        atolTransactionMessageService.processMessage(atolMessage)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(PodProcessElement.RECEIVE_ATOL_SUCCESS.code)
        rule.endProcess(processInstance)
    }

}
