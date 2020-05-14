package ru.iesorokin.payment.orchestrator.core.service

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.APPROVE_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.CANCELLED
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.COMPLETED
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.CONFIRMED
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.DEPOSIT_AND_GIVEAWAY_DONE
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.DEPOSIT_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.EXPIRED
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.GIVEAWAY_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.HOLD
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.NEW
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PARTIALLY_REFUND
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.REFUND
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.UNHOLD
import ru.iesorokin.payment.orchestrator.core.exception.NoSuitablePaymentStatusForRefund
import kotlin.test.assertFailsWith

class ValidationServiceTest {

    private val validationService = ValidationService()

    @ParameterizedTest
    @MethodSource(value = ["illegalTaskStatuses"])
    fun `should not validate task status`(taskStatus: PaymentTransactionStatus) {
        assertFailsWith(NoSuitablePaymentStatusForRefund::class) {
            validationService.checkPaymentStatusForRefund("test", taskStatus)
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["suitableTaskStatuses"])
    fun `should validate task status`(taskStatus: PaymentTransactionStatus) {
        validationService.checkPaymentStatusForRefund("test", taskStatus)
    }

    companion object {
        @JvmStatic
        fun suitableTaskStatuses() = listOf(
                PAID,
                REFUND,
                COMPLETED
        )

        @JvmStatic
        fun illegalTaskStatuses() = listOf(
                NEW,
                CONFIRMED,
                HOLD,
                UNHOLD,
                DEPOSIT_IN_PROGRESS,
                PARTIALLY_REFUND,
                EXPIRED,
                CANCELLED,
                APPROVE_IN_PROGRESS,
                DEPOSIT_AND_GIVEAWAY_DONE,
                GIVEAWAY_IN_PROGRESS
        )

    }

}
