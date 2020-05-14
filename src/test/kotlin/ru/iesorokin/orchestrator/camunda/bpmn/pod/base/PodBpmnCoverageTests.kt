package ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.base

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.ChangePaymentTaskStatusTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.CreatePaymentGiveAwayTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.PlaceTpNetDepositTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.ReceiveAtolSuccessTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.ReceiveTpnetDepositTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.RunGiveAwayProcessActivityTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.SaveDataFromAtolTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.SendFiscalizationStatusTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.UpdateTaskByRegisteredTransactionTaskTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.RegisterTransactionInAtolTaskTest

@RunWith(Suite::class)
@SuiteClasses(value = [
    ChangePaymentTaskStatusTaskTest::class,
    SaveDataFromAtolTaskTest::class,
    PlaceTpNetDepositTaskTest::class,
    ReceiveAtolSuccessTaskTest::class,
    ReceiveTpnetDepositTest::class,
    CreatePaymentGiveAwayTaskTest::class,
    SendFiscalizationStatusTaskTest::class,
    RunGiveAwayProcessActivityTest::class,
    RegisterTransactionInAtolTaskTest::class,
    UpdateTaskByRegisteredTransactionTaskTest::class
])
class PodBpmnCoverageTests