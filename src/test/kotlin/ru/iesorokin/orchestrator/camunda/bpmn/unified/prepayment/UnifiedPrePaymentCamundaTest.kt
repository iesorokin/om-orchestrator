package ru.iesorokin.orchestrator.camunda.bpmn.unified.prepayment

import org.camunda.bpm.engine.test.Deployment
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.orchestrator.camunda.bpmn.BaseCamundaTest

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/unified_prepayment.bpmn"])
@ContextConfiguration(classes = [
    // Process Engine config
    ProcessEngineTestCoverageRuleConfiguration::class
    // Tasks
])
abstract class UnifiedPrePaymentCamundaTest : BaseCamundaTest()