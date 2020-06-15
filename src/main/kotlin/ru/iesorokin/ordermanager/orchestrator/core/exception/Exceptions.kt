package ru.iesorokin.ordermanager.orchestrator.core.exception

class CamundaProcessNotCorrectStateException(override val message: String) : RuntimeException()


class EmptyFieldException(override val message: String) : RuntimeException()

class NoSuitablePaymentStatusForRefund(override val message: String) : RuntimeException()

class StartProcessException(override val message: String, override val cause: Throwable? = null) : RuntimeException()