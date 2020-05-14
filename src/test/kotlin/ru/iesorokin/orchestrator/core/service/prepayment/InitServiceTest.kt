package ru.iesorokin.payment.orchestrator.core.service.prepayment

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.exception.NullValueException
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.task.prepayment.SaveWorkflowIdTask
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.StartProcessMessage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

class InitServiceTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var initService: InitService
    @MockBean
    private lateinit var saveWorkflowIdTask: SaveWorkflowIdTask


    private val extOrderId = "12345678"
    private val paymentTaskId = "87654321"

    @Test
    fun `should init process with start message`() {
        val message = StartProcessMessage(SBERBANK_PREPAYMENT_WITH_TPNET, extOrderId, paymentTaskId)
        doNothing().whenever(saveWorkflowIdTask).execute(any())

        initService.initPrepaymentProcess(message)

        val processInstance = camundaService.findProcessInstanceByVariable(PAYMENT_TASK_ID, paymentTaskId)

        BpmnAwareAssertions.assertThat(processInstance).isStarted
        endProcess(processInstance)
    }

    @Test
    fun `shouldn't init process with wrong process definition key`() {
        try {
            initService.initProcess("WRONG TYPE", context())
            fail()
        } catch (e: Exception) {
            Assertions.assertThat(e).isInstanceOf(NullValueException::class.java)
        }
        assertNull(camundaService.findProcessInstanceByVariable(PAYMENT_TASK_ID, paymentTaskId))
    }

    @Test
    fun `shouldn't init second process with start message`() {
        val messageWorks = StartProcessMessage(SBERBANK_PREPAYMENT_WITH_TPNET, extOrderId, paymentTaskId)
        val messageIgnored = StartProcessMessage("IGNORED TYPE", extOrderId, paymentTaskId)
        doNothing().whenever(saveWorkflowIdTask).execute(any())

        initService.initPrepaymentProcess(messageWorks)
        initService.initPrepaymentProcess(messageIgnored)

        val processInstance = camundaService.findProcessInstanceByVariable(PAYMENT_TASK_ID, paymentTaskId)
        BpmnAwareAssertions.assertThat(processInstance).hasProcessDefinitionKey(SBERBANK_PREPAYMENT_WITH_TPNET)
        endProcess(processInstance)
    }

    @Test
    fun `should init process with process key and context`() {
        //When
        doNothing().whenever(saveWorkflowIdTask).execute(any())
        initService.initProcess(SBERBANK_PREPAYMENT_WITH_TPNET, context())

        //Then
        val processInstance = camundaService.findProcessInstanceByVariable(PAYMENT_TASK_ID, paymentTaskId)
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        endProcess(processInstance)
    }

    @Test
    fun `shouldn't init and detect two running processes in the context`() {
        val messageIgnored = StartProcessMessage(SBERBANK_PREPAYMENT_WITH_TPNET, extOrderId, paymentTaskId)

        doNothing().whenever(saveWorkflowIdTask).execute(any())
        val context = context()

        val processInstance = camundaService.startProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET, context)
        val processInstance2 = camundaService.startProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET, context)

        val beforeProcessInstanceList = rule.runtimeService.createProcessInstanceQuery()
                .active()
                .variableValueEquals(PAYMENT_TASK_ID, paymentTaskId).list()

        try {
            initService.initPrepaymentProcess(messageIgnored)
            fail()
        } catch (e: Exception) {
            Assertions.assertThat(e).isInstanceOf(ProcessEngineException::class.java)
        }

        val afterProcessInstanceList = rule.runtimeService.createProcessInstanceQuery()
                .active()
                .variableValueEquals(PAYMENT_TASK_ID, paymentTaskId).list()

        assertEquals(beforeProcessInstanceList.size, afterProcessInstanceList.size)
        assertFailsWith(
                exceptionClass = ProcessEngineException::class,
                block = {
                    camundaService.findProcessInstanceByVariable(PAYMENT_TASK_ID, messageIgnored.paymentTaskId)
                }
        )
        endProcess(processInstance)
        endProcess(processInstance2)

    }

    private fun context() = mapOf(
            PAYMENT_TASK_ID to paymentTaskId as Any,
            EXT_ORDER_ID to extOrderId as Any)
}
