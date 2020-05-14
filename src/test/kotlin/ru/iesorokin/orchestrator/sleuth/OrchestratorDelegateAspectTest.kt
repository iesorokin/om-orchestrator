package ru.iesorokin.orchestrator.sleuth

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.aspectj.lang.JoinPoint
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl
import org.camunda.bpm.engine.variable.value.StringValue
import org.junit.Test
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.sleuth.SleuthEnum.PROCESS_ID
import ru.iesorokin.orchestrator.sleuth.SleuthEnum.SOLUTION_ID
import ru.iesorokin.utility.sleuthbase.MdcService

class OrchestratorDelegateAspectTest {
    private val mdcService = mock<MdcService>()
    private val delegate = OrchestratorDelegateAspect(mdcService)

    @Test
    fun `should successfully set data to Mdc`() {
        val extractProcessDataFromDelegate = mock<ExtractProcessDataFromDelegate>()
        val processInstanceId = "processId"
        val solutionId = "solutionId"
        val process = mock<ExecutionEntity> {
            on { this.processInstanceId } doReturn processInstanceId
            on { getVariableTyped<StringValue>(EXT_ORDER_ID) } doReturn PrimitiveTypeValueImpl.StringValueImpl(solutionId)
        }
        val joinPoint = mock<JoinPoint> {
            on { args } doReturn listOf(process).toTypedArray()
        }

        delegate.putProcessDataInMdc(joinPoint, extractProcessDataFromDelegate)

        verify(mdcService).propagateMdc(PROCESS_ID.type, processInstanceId)
        verify(mdcService).propagateMdc(SOLUTION_ID.type, solutionId)
    }

    @Test
    fun `should not throw exception then fail`() {
        val extractProcessDataFromDelegate = mock<ExtractProcessDataFromDelegate>()
        val joinPoint = mock<JoinPoint> {
            on { args } doThrow NullPointerException()
        }

        delegate.putProcessDataInMdc(joinPoint, extractProcessDataFromDelegate)

        verifyNoMoreInteractions(mdcService)
    }

    @Test
    fun `should not throw exception if no available ExecutionEntity`() {
        val extractProcessDataFromDelegate = mock<ExtractProcessDataFromDelegate>()

        val process = mock<Any>()
        val joinPoint = mock<JoinPoint> {
            on { args } doReturn listOf(process).toTypedArray()
        }

        delegate.putProcessDataInMdc(joinPoint, extractProcessDataFromDelegate)

        verifyNoMoreInteractions(mdcService)
    }

}
