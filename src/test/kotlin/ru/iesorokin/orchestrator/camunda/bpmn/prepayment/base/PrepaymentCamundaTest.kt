package ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base

import org.camunda.bpm.engine.test.Deployment
import org.junit.runner.RunWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ContextConfiguration
import ru.iesorokin.bpmn.test.config.ProcessEngineTestCoverageRuleConfiguration
import ru.iesorokin.orchestrator.bpmn.test.CamundaCoverageSpringJUnit4ClassRunner
import ru.iesorokin.orchestrator.camunda.bpmn.BaseCamundaTest
import ru.iesorokin.orchestrator.core.service.AtolService
import ru.iesorokin.orchestrator.core.service.ItsmService
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.service.SolutionService
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.service.prepayment.SberbankDepositService
import ru.iesorokin.orchestrator.core.service.refund.RefundService
import ru.iesorokin.orchestrator.core.task.common.ChangePaymentTaskStatusTask
import ru.iesorokin.orchestrator.core.task.common.RegisterAtolTransactionTask
import ru.iesorokin.orchestrator.core.task.common.RegisterTransactionInAtolTask
import ru.iesorokin.orchestrator.core.task.common.SaveDataFromAtolTask
import ru.iesorokin.orchestrator.core.task.common.UpdateTaskByRegisteredTransactionTask
import ru.iesorokin.orchestrator.core.task.prepayment.ConfirmSberbankTask
import ru.iesorokin.orchestrator.core.task.prepayment.CreateTpnetItsmTicketTask
import ru.iesorokin.orchestrator.core.task.prepayment.PlaceTpNetDepositTask
import ru.iesorokin.orchestrator.core.task.prepayment.SaveWorkflowIdTask
import ru.iesorokin.orchestrator.core.task.prepayment.StartRefundProcessTask
import ru.iesorokin.orchestrator.output.client.payment.task.PaymentTaskClient
import ru.iesorokin.orchestrator.output.stream.sender.SberbankDepositCommandMessageSender

@RunWith(CamundaCoverageSpringJUnit4ClassRunner::class)
@Deployment(resources = ["bpmn/sberbank_pre_payment_with_tpnet.bpmn"])
@ContextConfiguration(classes = [
    SaveWorkflowIdTask::class,
    SaveDataFromAtolTask::class,
    RegisterAtolTransactionTask::class,
    ProcessEngineTestCoverageRuleConfiguration::class,
    ConfirmSberbankTask::class,
    StartRefundProcessTask::class,
    PlaceTpNetDepositTask::class,
    ChangePaymentTaskStatusTask::class,
    CreateTpnetItsmTicketTask::class,
    RegisterTransactionInAtolTask::class,
    UpdateTaskByRegisteredTransactionTask::class
])
abstract class PrepaymentCamundaTest : BaseCamundaTest() {

    @MockBean
    protected lateinit var solutionService: SolutionService
    @MockBean
    protected lateinit var atolService: AtolService
    @SpyBean
    protected lateinit var registerAtolTransactionTask: RegisterAtolTransactionTask

    @MockBean
    lateinit var paymentTaskService: PaymentTaskService
    @MockBean
    lateinit var refundService: RefundService
    @MockBean
    lateinit var sberbankDepositService: SberbankDepositService
    @MockBean
    lateinit var itsmService: ItsmService
    @MockBean
    lateinit var tpNetService: TpNetService
    @MockBean
    lateinit var sberbankDepositCommandSender: SberbankDepositCommandMessageSender
    @MockBean
    lateinit var paymentTaskClient: PaymentTaskClient
}
