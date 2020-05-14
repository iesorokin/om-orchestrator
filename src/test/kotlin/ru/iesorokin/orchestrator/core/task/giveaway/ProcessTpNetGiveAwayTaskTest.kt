package ru.iesorokin.orchestrator.core.task.giveaway

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl
import org.camunda.bpm.engine.variable.value.StringValue
import org.junit.Test
import ru.iesorokin.orchestrator.core.service.TpNetService

const val paymentTaskId = "paymentTaskId"

class ProcessTpNetGiveAwayTaskTest {
    private val tpNetService = mock<TpNetService>()
    private val execution = mock<DelegateExecution>()
    private val processTpNetGiveAwayTask = ProcessTpNetGiveAwayTask(tpNetService)

    @Test
    fun `execute should call give Away`() {
        val paymentTaskValue = "paymentTaskValue"

        whenever(execution.getVariableTyped<StringValue>((paymentTaskId)))
                .thenReturn(PrimitiveTypeValueImpl.StringValueImpl(paymentTaskValue))
        processTpNetGiveAwayTask.execute(execution)
        verify(tpNetService, times(1)).doGiveAway(paymentTaskValue)
    }

}