package ru.iesorokin.payment.orchestrator.camunda.bpmn.giveaway

import org.camunda.bpm.engine.test.Deployment
import org.junit.runner.RunWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.payment.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.payment.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.payment.orchestrator.camunda.bpmn.BaseCamundaTest
import ru.iesorokin.payment.orchestrator.core.service.AtolService
import ru.iesorokin.payment.orchestrator.core.service.TpNetService
import ru.iesorokin.payment.orchestrator.core.task.giveaway.ProcessTpNetGiveAwayTask
import ru.iesorokin.payment.orchestrator.core.task.giveaway.RegisterAtolGiveAwayTask

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/give_away.bpmn"])
@ContextConfiguration(classes = [
    ProcessEngineTestCoverageRuleConfiguration::class,
    ProcessTpNetGiveAwayTask::class,
    RegisterAtolGiveAwayTask::class
])
abstract class GiveAwayCamundaTest: BaseCamundaTest() {

    @MockBean
    protected lateinit var tpNetService: TpNetService

    @MockBean
    protected lateinit var atolService: AtolService

}