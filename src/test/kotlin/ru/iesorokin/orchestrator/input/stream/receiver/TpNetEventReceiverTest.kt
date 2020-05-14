package ru.iesorokin.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.orchestrator.core.enums.bpmn.RefundEventType
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.service.giveaway.GiveAwayService
import ru.iesorokin.orchestrator.core.service.refund.RefundTpNetService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.TpNetDepositEventMessage
import ru.iesorokin.orchestrator.input.stream.receiver.dto.TpNetGiveAwayEventMessage
import ru.iesorokin.orchestrator.input.stream.receiver.dto.TpNetRefundEventMessage

class TpNetEventReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var tpNetService: TpNetService
    @MockBean
    private lateinit var refundTpNetService: RefundTpNetService
    @MockBean
    private lateinit var giveAwayService: GiveAwayService

    private val paymentTaskId = "13123"

    @Test
    fun `handleTpnetDepositFailEventMessage should handle FAIL`() {
        sendFailDepositMessage()

        verify(tpNetService).handleDepositEvent(paymentTaskId, DepositEventType.FAIL)
        verifyNoMoreInteractions(tpNetService)
    }

    @Test
    fun `handleTpnetDepositFailEventMessage should not handle if exceed maxRetry`() {
        sendFailDepositMessage(100)
        verifyZeroInteractions(tpNetService)
    }

    @Test
    fun `handleTpnetDepositSuccessEventMessage should handle SUCCESS`() {
        sendSuccessDepositMessage()

        verify(tpNetService).handleDepositEvent(paymentTaskId, DepositEventType.SUCCESS)
        verifyNoMoreInteractions(tpNetService)
    }

    @Test
    fun `handleTpnetDepositSuccessEventMessage should not handle if exceed maxRetry`() {
        sendSuccessDepositMessage(100)
        verifyZeroInteractions(tpNetService)
    }

    @Test
    fun `handleTpnetRefundFailEventMessage should handle FAIL`() {
        sendFailRefundMessage()

        verify(refundTpNetService).handleRefundEvent(paymentTaskId, RefundEventType.FAIL)
        verifyNoMoreInteractions(tpNetService)
    }

    @Test
    fun `handleTpnetRefundFailEventMessage should not handle if exceed maxRetry`() {
        sendFailRefundMessage(100)

        verifyZeroInteractions(refundTpNetService)
    }

    @Test
    fun `handleTpnetRefundSuccessEventMessage should handle SUCCESS`() {
        sendSuccessRefundMessage()

        verify(refundTpNetService).handleRefundEvent(paymentTaskId, RefundEventType.SUCCESS)
        verifyNoMoreInteractions(refundTpNetService)
    }

    @Test
    fun `handleTpnetRefundSuccessEventMessage should not handle if exceed maxRetry`() {
        sendSuccessRefundMessage(100)
        verifyZeroInteractions(refundTpNetService)
    }

    @Test
    fun `handleTpnetGiveAwaySuccessEventMessage - ok`() {
        //When
        sendSuccessGiveAwayMessage()

        //Then
        verify(giveAwayService, times(1)).processSuccessGiveAway(paymentTaskId)
    }

    @Test
    fun `handleTpnetGiveAwaySuccessEventMessage - not_ok - exceeded max retry`() {
        //When
        sendSuccessGiveAwayMessage(100)

        //Then
        verifyZeroInteractions(giveAwayService)
    }

    @Test
    fun `handleTpnetGiveAwayFailEventMessage - ok`() {
        //When
        sendFailGiveAwayMessage()

        //Then
        verify(giveAwayService, times(1)).processFailedGiveAway(paymentTaskId)
    }

    @Test
    fun `handleTpnetGiveAwayFailEventMessage - not_ok - exceeded max retry`() {
        //When
        sendFailGiveAwayMessage(100)

        //Then
        verifyZeroInteractions(giveAwayService)
    }

    private fun sendFailDepositMessage(count: Long? = 0) =
            messagingSource.tpNetTransactionCommandFailedInput().send(depositMessage(count))

    private fun sendSuccessDepositMessage(count: Long? = 0) =
            messagingSource.tpNetTransactionCommandSuccessInput().send(depositMessage(count))

    private fun depositMessage(count: Long? = 0) =
            MessageBuilder
                    .withPayload(TpNetDepositEventMessage(paymentTaskId))
                    .setHeader(X_DEATH_HEADER, mapOf("count" to count))
                    .build()

    private fun sendFailRefundMessage(count: Long? = 0) =
            messagingSource.tpNetRefundCommandFailedInput().send(refundMessage(count))

    private fun sendSuccessRefundMessage(count: Long? = 0) =
            messagingSource.tpNetRefundCommandSuccessInput().send(refundMessage(count))

    private fun refundMessage(count: Long? = 0) =
            MessageBuilder
                    .withPayload(TpNetRefundEventMessage(paymentTaskId))
                    .setHeader(X_DEATH_HEADER, mapOf("count" to count))
                    .build()

    private fun sendFailGiveAwayMessage(count: Long? = 0) =
            messagingSource.tpnetGiveAwayFailedInput().send(giveAwayMessage(count))

    private fun sendSuccessGiveAwayMessage(count: Long? = 0) =
            messagingSource.tpnetGiveAwaySuccessInput().send(giveAwayMessage(count))

    private fun giveAwayMessage(count: Long? = 0) =
            MessageBuilder
                    .withPayload(TpNetGiveAwayEventMessage(paymentTaskId))
                    .setHeader(X_DEATH_HEADER, mapOf("count" to count))
                    .build()

}
