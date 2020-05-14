package ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent.base

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent.ChangePaymentTaskStatusTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent.CreatePaymentGiveAwayTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent.ReceivePaidStatusTest

@RunWith(Suite::class)
@SuiteClasses(value = [
    ChangePaymentTaskStatusTaskTest::class,
    CreatePaymentGiveAwayTaskTest::class,
    ReceivePaidStatusTest::class
])
class PodAgentBpmnCoverageTests