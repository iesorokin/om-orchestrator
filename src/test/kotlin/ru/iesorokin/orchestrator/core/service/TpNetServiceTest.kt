package ru.iesorokin.payment.orchestrator.core.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import ru.iesorokin.payment.orchestrator.output.stream.sender.TpNetSender
import ru.iesorokin.utility.sleuthbase.MdcService

class TpNetServiceTest {
    private val tpNetSender = mock<TpNetSender>()
    private val camundaService = mock<CamundaService>()
    private val mdcService = mock<MdcService>()
    private val tpNetGiveAwayService = TpNetService(tpNetSender, camundaService, mdcService)

    @Test
    fun `doGiveAway should send giveAway command message`() {
        val paymentTaskId = "paymentTaskId"

        tpNetGiveAwayService.doGiveAway(paymentTaskId)

        verify(tpNetSender).sendGiveAwayCommand(paymentTaskId)
    }
}