package ru.iesorokin.payment.orchestrator.output.client

import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import ru.iesorokin.payment.orchestrator.core.enums.ErrorCode
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

class ClientErrorHandler : ResponseErrorHandler {
    private val errorCodeBySeriesMap = mutableMapOf<HttpStatus.Series, ErrorCode>()
    private val defaultErrorCode: ErrorCode

    constructor(errorCode4xxSeries: ErrorCode, errorCode5xxSeries: ErrorCode, defaultErrorCode: ErrorCode) {
        this.errorCodeBySeriesMap[HttpStatus.Series.CLIENT_ERROR] = errorCode4xxSeries
        this.errorCodeBySeriesMap[HttpStatus.Series.SERVER_ERROR] = errorCode5xxSeries
        this.defaultErrorCode = defaultErrorCode
    }

    override fun hasError(response: ClientHttpResponse): Boolean {
        return HttpStatus.Series.valueOf(response.statusCode) != HttpStatus.Series.SUCCESSFUL
    }

    override fun handleError(response: ClientHttpResponse) {
        try {
            log.info(IOUtils.toString(response.body, StandardCharsets.UTF_8))
        } catch (e: Exception) {
            log.error(e.message, e)
        }

        val errorCode = resolveErrorCode(response.statusCode)
        val message = getErrorMessage(errorCode, response.statusCode)
        throw ClientResponseErrorException(message, errorCode, response.statusCode)
    }

    private fun resolveErrorCode(responseStatus: HttpStatus) = errorCodeBySeriesMap
            .getOrDefault(responseStatus.series(), defaultErrorCode)

    private fun getErrorMessage(errorCode: ErrorCode, httpStatus: HttpStatus) =
            String.format("(errorCode=%s, errorMessage=%s, httpStatus=%s)", errorCode.code, errorCode.errorMessage, httpStatus)
}

class ClientResponseErrorException(
        override val message: String,
        val errorCode: ErrorCode,
        val httpStatus: HttpStatus
) : RuntimeException()
