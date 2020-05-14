package ru.iesorokin.orchestrator.camunda.bpmn.refund.base

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import ru.iesorokin.orchestrator.camunda.bpmn.refund.AtolRefundSuccessMessageTest
import ru.iesorokin.orchestrator.camunda.bpmn.refund.CheckPaymentStatusTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.refund.RefundSberbankPaymentTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.refund.RegisterRefundAtolTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.refund.SaveFiscalDataTaskTest
import ru.iesorokin.orchestrator.camunda.bpmn.refund.SendRefundSmsTaskTest

@RunWith(Suite::class)
@SuiteClasses(value = [
    RegisterRefundAtolTaskTest::class,
    AtolRefundSuccessMessageTest::class,
    RegisterRefundAtolTaskTest::class,
    CheckPaymentStatusTaskTest::class,
    SaveFiscalDataTaskTest::class,
    RefundSberbankPaymentTaskTest::class,
    SaveFiscalDataTaskTest::class,
    RefundSberbankPaymentTaskTest::class,
    SendRefundSmsTaskTest::class
])
class RefundBpmnCoverageTests
