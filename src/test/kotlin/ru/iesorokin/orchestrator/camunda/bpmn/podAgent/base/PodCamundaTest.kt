package ru.iesorokin.orchestrator.camunda.bpmn.podAgent.base

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
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.POD_AGENT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.task.common.ChangePaymentTaskStatusTask
import ru.iesorokin.orchestrator.core.task.pod.CreatePaymentGiveAwayTask

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/pod_agent.bpmn", "bpmn/common/tp_net_interaction.bpmn"])
@ContextConfiguration(classes = [
    // Process Engine config
    ProcessEngineTestCoverageRuleConfiguration::class,
    // Tasks
    ChangePaymentTaskStatusTask::class,
    CreatePaymentGiveAwayTask::class
])
abstract class PodAgentCamundaTest : BaseCamundaTest() {

    val testPaymentTaskId = "12345678"
    private val testSolutionId = "solutionId"

    @MockBean
    protected lateinit var paymentTaskService: PaymentTaskService

    @MockBean
    protected lateinit var tpNetService: TpNetService

    fun startPodAgentProcess(startBefore: PodAgentProcessElement, variablesMap: Map<String, Any>? = null) {
        val processInstanceBuilder = rule.runtimeService
                .createProcessInstanceByKey(POD_AGENT.processName)
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