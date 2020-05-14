package ru.iesorokin.orchestrator.camunda.bpmn.tpNetInteraction.base

import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.runner.RunWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.orchestrator.camunda.bpmn.BaseCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement
import ru.iesorokin.orchestrator.core.service.ItsmService
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.task.giveaway.ProcessTpNetGiveAwayTask
import ru.iesorokin.orchestrator.core.task.prepayment.CreateTpnetItsmTicketTask
import ru.iesorokin.orchestrator.core.task.prepayment.PlaceTpNetDepositTask

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/common/tp_net_interaction.bpmn"])
@ContextConfiguration(classes = [
    // Process Engine config
    ProcessEngineTestCoverageRuleConfiguration::class,
    // Tasks
    PlaceTpNetDepositTask::class,
    ProcessTpNetGiveAwayTask::class,
    CreateTpnetItsmTicketTask::class
])
abstract class TpNetInteractionCamundaTest : BaseCamundaTest() {

    val testPaymentTaskId = "12345678"
    val testSolutionId = "solutionId"

    @MockBean
    protected lateinit var tpNetService: TpNetService
    @MockBean
    protected lateinit var itsmService: ItsmService


    fun startTpNetInteractionProcess(startBefore: TpNetInteractionProcessElement, variablesMap: Map<String, Any>? = null) {
        val processInstanceBuilder = rule.runtimeService
                .createProcessInstanceByKey(Process.TP_NET_INTERACTION.processName)
                .startBeforeActivity(startBefore.code)
                .setVariable(PAYMENT_TASK_ID, testPaymentTaskId)
                .setVariable(EXT_ORDER_ID, testSolutionId)

        variablesMap?.forEach {
            processInstanceBuilder.setVariable(it.key, it.value)
        }

        processInstance = processInstanceBuilder.execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

}