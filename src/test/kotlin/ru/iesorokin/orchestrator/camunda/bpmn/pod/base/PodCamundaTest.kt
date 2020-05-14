package ru.iesorokin.orchestrator.camunda.bpmn.pod.base

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
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.orchestrator.core.service.AtolService
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.service.SolutionService
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.task.common.ChangePaymentTaskStatusTask
import ru.iesorokin.orchestrator.core.task.common.RegisterAtolTransactionTask
import ru.iesorokin.orchestrator.core.task.common.RegisterTransactionInAtolTask
import ru.iesorokin.orchestrator.core.task.common.SaveDataFromAtolTask
import ru.iesorokin.orchestrator.core.task.common.UpdateTaskByRegisteredTransactionTask
import ru.iesorokin.orchestrator.core.task.pod.CreatePaymentGiveAwayTask
import ru.iesorokin.orchestrator.core.task.pod.SendFiscalizationStatusTask
import ru.iesorokin.orchestrator.core.task.prepayment.PlaceTpNetDepositTask

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/pod_payment.bpmn", "bpmn/give_away.bpmn"])
@ContextConfiguration(classes = [
    // Process Engine config
    ProcessEngineTestCoverageRuleConfiguration::class,
    // Tasks
    ChangePaymentTaskStatusTask::class,
    RegisterAtolTransactionTask::class,
    SaveDataFromAtolTask::class,
    PlaceTpNetDepositTask::class,
    CreatePaymentGiveAwayTask::class,
    SendFiscalizationStatusTask::class,
    RegisterTransactionInAtolTask::class,
    UpdateTaskByRegisteredTransactionTask::class
])
abstract class PodCamundaTest : BaseCamundaTest() {

    val testPaymentTaskId = "12345678"
    val testSolutionId = "solutionId"

    @MockBean
    protected lateinit var paymentTaskService: PaymentTaskService

    @MockBean
    protected lateinit var tpNetService: TpNetService

    @MockBean
    protected lateinit var atolService: AtolService

    @MockBean
    protected lateinit var solutionService: SolutionService

    fun startPodProcess(startBefore: PodProcessElement, variablesMap: Map<String, Any>? = null) {
        val processInstanceBuilder = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
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