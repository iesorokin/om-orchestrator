package ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.base

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.CreateTpNetItsmTicketTasksTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.PlaceTpNetTasksTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.ReceiveTpNetTasksTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.ResolveSequenceTest

@RunWith(Suite::class)
@SuiteClasses(value = [
    ResolveSequenceTest::class,
    CreateTpNetItsmTicketTasksTest::class,
    ReceiveTpNetTasksTest::class,
    PlaceTpNetTasksTest::class
])
class TpNetInteractionBpmnCoverageTests