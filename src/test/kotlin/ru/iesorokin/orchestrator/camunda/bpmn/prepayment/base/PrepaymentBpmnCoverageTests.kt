package ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.ApprovePaymentMessageTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.ConfirmSberbankTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.PaymentStatusDepositInProgressTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.PaymentStatusExpiredTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.PaymentStatusHoldTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.PlaceTpNetDepositTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.ReceiveAtolRegisterMessageTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.ReceiveProcessCancellationMessageTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.ReceiveSberbankPaymentProcessTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.SaveDataFromAtolTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.SaveWorkflowIdTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.StartRefundProcessTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.UpdateTaskByRegisteredTransactionTaskTest

@RunWith(Suite::class)
@SuiteClasses(value = [
    ReceiveSberbankPaymentProcessTest::class,
    ApprovePaymentMessageTest::class,
    ReceiveAtolRegisterMessageTest::class,
    PaymentStatusExpiredTaskTest::class,
    PaymentStatusHoldTaskTest::class,
    PaymentStatusDepositInProgressTaskTest::class,
    SaveWorkflowIdTaskTest::class,
    ApprovePaymentMessageTest::class,
    ConfirmSberbankTaskTest::class,
    PlaceTpNetDepositTaskTest::class,
    SaveDataFromAtolTaskTest::class,
    StartRefundProcessTaskTest::class,
    ReceiveProcessCancellationMessageTest::class,
    UpdateTaskByRegisteredTransactionTaskTest::class
])
class PrepaymentBpmnCoverageTests
