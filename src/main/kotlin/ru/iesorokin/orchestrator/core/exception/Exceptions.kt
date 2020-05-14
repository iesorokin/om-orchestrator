package ru.iesorokin.payment.orchestrator.core.exception

class CamundaProcessNotCorrectStateException(override val message: String) : RuntimeException()

class PaymentTaskNotFoundException(override val message: String) : RuntimeException()

class GiveAwaysNotFoundException(override val message: String) : RuntimeException()

class PaymentClientException(override val message: String) : RuntimeException()

class EmptyFieldException(override val message: String) : RuntimeException()

class SolutionNotFoundException(override val message: String) : RuntimeException()

class EmptyAtolResponseException(override val message: String) : RuntimeException()

class SberbankRefundException(override val message: String, override val cause: Throwable? = null) : RuntimeException()

class ErrorSendSmsMessageException(override val message: String) : RuntimeException()

class InvalidLineTypeException(override val message: String) : RuntimeException()

class InvalidUnitAmountIncludingVatException(override val message: String) : RuntimeException()

class InvalidTaskStatusException(override val message: String) : RuntimeException()

class LineNotFoundException(override val message: String) : RuntimeException()

class NoSuitablePaymentStatusForRefund(override val message: String) : RuntimeException()

class StartProcessException(override val message: String, override val cause: Throwable? = null) : RuntimeException()
