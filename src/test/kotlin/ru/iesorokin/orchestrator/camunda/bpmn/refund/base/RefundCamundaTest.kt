package ru.iesorokin.payment.orchestrator.camunda.bpmn.refund.base

import org.camunda.bpm.engine.test.Deployment
import org.junit.runner.RunWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.payment.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.payment.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.payment.orchestrator.camunda.bpmn.BaseCamundaTest
import ru.iesorokin.payment.orchestrator.core.service.AtolService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.core.service.SmsService
import ru.iesorokin.payment.orchestrator.core.service.SolutionService
import ru.iesorokin.payment.orchestrator.core.service.ValidationService
import ru.iesorokin.payment.orchestrator.core.service.refund.RefundContextService
import ru.iesorokin.payment.orchestrator.core.service.refund.RefundSberbankService
import ru.iesorokin.payment.orchestrator.core.service.refund.RefundTpNetService
import ru.iesorokin.payment.orchestrator.core.task.refund.CheckPaymentStatusTask
import ru.iesorokin.payment.orchestrator.core.task.refund.RefundSberbankPaymentTask
import ru.iesorokin.payment.orchestrator.core.task.refund.RefundTpNetTask
import ru.iesorokin.payment.orchestrator.core.task.refund.RegisterRefundAtolTask
import ru.iesorokin.payment.orchestrator.core.task.refund.SaveFiscalDataTask
import ru.iesorokin.payment.orchestrator.core.task.refund.SendRefundSmsTask

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/sberbank_refund_with_tpnet.bpmn"])
@ContextConfiguration(classes = [
    ProcessEngineTestCoverageRuleConfiguration::class,
    RegisterRefundAtolTask::class,
    RefundSberbankPaymentTask::class,
    RefundTpNetTask::class,
    SaveFiscalDataTask::class,
    RefundTpNetTask::class,
    CheckPaymentStatusTask::class,
    SendRefundSmsTask::class
])
abstract class RefundCamundaTest : BaseCamundaTest() {

    @MockBean
    internal lateinit var paymentTaskService: PaymentTaskService
    @SpyBean
    internal lateinit var validationService: ValidationService
    @MockBean
    internal lateinit var solutionService: SolutionService
    @MockBean
    lateinit var refundContextService: RefundContextService

    @MockBean
    internal lateinit var atolService: AtolService
    @MockBean
    internal lateinit var refundSberbankService: RefundSberbankService
    @MockBean
    internal lateinit var refundTpNetService: RefundTpNetService
    @MockBean
    internal lateinit var smsService: SmsService
}