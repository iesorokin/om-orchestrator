package ru.iesorokin.payment.orchestrator.camunda.bpmn.unified.prepayment

import org.camunda.bpm.engine.test.Deployment
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.payment.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.payment.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.payment.orchestrator.camunda.bpmn.BaseCamundaTest

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/unified_prepayment.bpmn"])
@ContextConfiguration(classes = [
    // Process Engine config
    ProcessEngineTestCoverageRuleConfiguration::class
    // Tasks
])
abstract class UnifiedPrePaymentCamundaTest : BaseCamundaTest()