package ru.iesorokin.orchestrator.camunda

import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.iesorokin.bpmn.test.config.ProcessEngineRuleConfiguration
import ru.iesorokin.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.orchestrator.core.service.CamundaService

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@Import(ProcessEngineRuleConfiguration::class)
@Deployment(resources = [
    "bpmn/sberbank_pre_payment_with_tpnet.bpmn",
    "bpmn/sberbank_refund_with_tpnet.bpmn",
    "bpmn/give_away.bpmn"
])
abstract class BaseSpringBootWithCamundaTest {
    companion object {
        @Rule
        @ClassRule
        @JvmStatic
        lateinit var rule: ProcessEngineRule

        lateinit var camundaService: CamundaService

        @BeforeClass
        @JvmStatic
        fun enableProcessEngine() {
            camundaService = CamundaService(rule.runtimeService)
        }
    }

    @After
    fun tearDownRule() {
        rule.runtimeService.createExecutionQuery().active().list().forEach {
            try {
                rule.runtimeService.deleteProcessInstance(it.processInstanceId, "end test")
            } catch (e: Exception) {}
        }
        val processList = rule.runtimeService.createExecutionQuery().active().list()
        Assertions.assertThat(processList).isEmpty()
    }

    fun endProcess(processInstance: ProcessInstance) {
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}

