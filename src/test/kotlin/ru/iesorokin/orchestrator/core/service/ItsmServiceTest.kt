package ru.iesorokin.orchestrator.core.service

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.output.stream.sender.ItsmSender

class ItsmServiceTest {
    private val itsmSender = mock<ItsmSender>()
    private val itsmService = ItsmService(itsmSender)

    @Test
    fun `createIncident should call sendItsmCreateIncidentCommandMessage`() {
        val paymentTaskId = "paymentTaskId"
        val processInstanceId = "processInstanceId"
        val operationType = "DEPOSIT"

        val testIncidentData = TpnetItsmTicket(
                paymentTaskId = paymentTaskId,
                processInstanceId = processInstanceId,
                tpnetOperationType = operationType
        )

        doNothing().`when`(itsmSender).sendTpnetItsmTicketMessage(testIncidentData)

        itsmService.createTicket(testIncidentData)

        verify(itsmSender).sendTpnetItsmTicketMessage(testIncidentData)
    }
}
