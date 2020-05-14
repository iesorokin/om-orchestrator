package ru.iesorokin.orchestrator.web

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ru.iesorokin.orchestrator.core.enums.ErrorCode
import ru.iesorokin.orchestrator.core.exception.InvalidLineTypeException
import ru.iesorokin.orchestrator.core.exception.InvalidTaskStatusException
import ru.iesorokin.orchestrator.core.exception.InvalidUnitAmountIncludingVatException
import ru.iesorokin.orchestrator.core.exception.LineNotFoundException
import ru.iesorokin.orchestrator.core.exception.PaymentClientException
import ru.iesorokin.orchestrator.core.exception.PaymentTaskNotFoundException
import ru.iesorokin.orchestrator.core.exception.StartProcessException

private val log = KotlinLogging.logger { }

@ControllerAdvice
@ResponseBody
class RestErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [PaymentTaskNotFoundException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handle(e: PaymentTaskNotFoundException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.PAYMENT_TASK_NOT_FOUND.toErrorResponse()
    }

    @ExceptionHandler(value = [InvalidLineTypeException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handle(e: InvalidLineTypeException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.LINE_TYPE_ERROR.toErrorResponse()
    }

    @ExceptionHandler(value = [InvalidUnitAmountIncludingVatException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handle(e: InvalidUnitAmountIncludingVatException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.VAT_ERROR.toErrorResponse()
    }

    @ExceptionHandler(value = [InvalidTaskStatusException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handle(e: InvalidTaskStatusException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.TASK_STATUS_ERROR.toErrorResponse()
    }

    @ExceptionHandler(value = [StartProcessException::class])
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handle(e: StartProcessException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.FAILED_START_PROCESS.toErrorResponse()
    }

    @ExceptionHandler(value = [PaymentClientException::class])
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handle(e: PaymentClientException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.PAYMENT_TASK_NOT_AVAILABLE.toErrorResponse()
    }

    @ExceptionHandler(value = [LineNotFoundException::class])
    @ResponseStatus(value = HttpStatus.CONFLICT)
    fun handle(e: LineNotFoundException): ErrorResponse {
        log.warn(e.message, e)
        return ErrorCode.LINE_NOT_EXIST.toErrorResponse()
    }

    data class ErrorResponse(val errors: Collection<ErrorDescription>)

    data class ErrorDescription(val code: Int,
                                val message: String)

    private fun ErrorCode.toErrorResponse() = ErrorResponse(listOf(ErrorDescription(code, errorMessage)))
}