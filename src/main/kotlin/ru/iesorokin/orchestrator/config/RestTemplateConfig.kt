package ru.iesorokin.orchestrator.config

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.zalando.logbook.Logbook
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor
import ru.iesorokin.orchestrator.core.enums.Application
import ru.iesorokin.orchestrator.core.enums.ErrorCode
import ru.iesorokin.orchestrator.output.client.ClientErrorHandler
import java.io.IOException

const val LEAD_APPLICATION = "Lead-application"

@Configuration
class RestTemplateConfig(private val logbook: Logbook,
                         @Value("\${internalSystem.default.maxConnPerRoute:25}")
                         private val maxConnPerRoute: Int,
                         @Value("\${internalSystem.default.maxConnTotal:50}")
                         private val maxConnTotal: Int) {

    @Bean
    @LoadBalanced
    fun restTemplatePaymentTask(restTemplateBuilder: RestTemplateBuilder,
                                @Value("\${payment.readTimeout:\${internalSystem.default.readTimeout}}")
                                readTimeout: Int,
                                @Value("\${payment.connectTimeout:\${internalSystem.default.connectTimeout}}")
                                connectTimeout: Int) =
            restTemplateBuilder
                    .requestFactory { createRequestFactory(readTimeout, connectTimeout) }
                    .errorHandler(responseErrorHandlerPaymentTask())
                    .build(RestTemplate::class.java)!!


    @Bean
    fun responseErrorHandlerPaymentTask() =
            ClientErrorHandler(ErrorCode.PAYMENT_TASK_ERROR, ErrorCode.PAYMENT_TASK_NOT_AVAILABLE, ErrorCode.PAYMENT_TASK_NOT_AVAILABLE)

    @Bean
    fun restTemplateSolution(restTemplateBuilder: RestTemplateBuilder,
                             @Value("\${solution.readTimeout:\${externalSystem.default.readTimeout}}")
                             readTimeout: Int,
                             @Value("\${solution.connectTimeout:\${externalSystem.default.connectTimeout}}")
                             connectTimeout: Int) =
            restTemplateBuilder
                    .requestFactory { createRequestFactory(readTimeout, connectTimeout) }
                    .errorHandler(responseErrorHandlerSolution())
                    .build(RestTemplate::class.java)!!

    @Bean
    fun responseErrorHandlerSolution() =
            ClientErrorHandler(ErrorCode.SOLUTION_ERROR, ErrorCode.SOLUTION_NOT_AVAILABLE, ErrorCode.SOLUTION_NOT_AVAILABLE)

    @Bean
    @LoadBalanced
    fun restTemplateAtol(restTemplateBuilder: RestTemplateBuilder,
                         @Value("\${payment.readTimeout:\${internalSystem.default.readTimeout}}")
                         readTimeout: Int,
                         @Value("\${payment.connectTimeout:\${internalSystem.default.connectTimeout}}")
                         connectTimeout: Int) =
            restTemplateBuilder
                    .requestFactory { createRequestFactory(readTimeout, connectTimeout) }
                    .errorHandler(responseErrorHandlerAtol())
                    .build(RestTemplate::class.java)!!

    @Bean
    @LoadBalanced
    fun cashRestTemplate(builder: RestTemplateBuilder): RestTemplate =
            builder.errorHandler(ErrorHandlerIgnore()).build()

    @Bean
    @LoadBalanced
    fun sberbankRestTemplate(builder: RestTemplateBuilder): RestTemplate =
            builder.errorHandler(ErrorHandlerIgnore()).build()

    @Bean
    fun responseErrorHandlerAtol() =
            ClientErrorHandler(ErrorCode.ATOL_ERROR, ErrorCode.ATOL_NOT_AVAILABLE, ErrorCode.ATOL_NOT_AVAILABLE)

    @Bean
    @LoadBalanced
    fun restTemplateSberbank(restTemplateBuilder: RestTemplateBuilder,
                             @Value("\${payment.readTimeout:\${internalSystem.default.readTimeout}}")
                             readTimeout: Int,
                             @Value("\${payment.connectTimeout:\${internalSystem.default.connectTimeout}}")
                             connectTimeout: Int) =
            restTemplateBuilder
                    .requestFactory { createRequestFactory(readTimeout, connectTimeout) }
                    .errorHandler(responseErrorHandlerSberbank())
                    .build(RestTemplate::class.java)!!


    @Bean
    fun responseErrorHandlerSberbank() =
            ClientErrorHandler(ErrorCode.SBERBANK_ERROR, ErrorCode.SBERBANK_NOT_AVAILABLE, ErrorCode.SBERBANK_NOT_AVAILABLE)

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = ["restTemplateSms"])
    fun restTemplateSms(restTemplateBuilder: RestTemplateBuilder,
                        @Value("\${sms.readTimeout:\${externalSystem.default.readTimeout}}")
                        readTimeout: Int,
                        @Value("\${sms.connectTimeout:\${externalSystem.default.connectTimeout}}")
                        connectTimeout: Int): RestTemplate {
        return restTemplateBuilder
                .interceptors(createSmsHeaderInterceptors())
                .requestFactory {
                    createRequestFactory(
                            readTimeout,
                            connectTimeout
                    )
                }
                .errorHandler(responseErrorHandlerSms())
                .build(RestTemplate::class.java)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["responseErrorHandlerSms"])
    fun responseErrorHandlerSms() =
            ClientErrorHandler(
                    ErrorCode.SMS_ERROR,
                    ErrorCode.SMS_NOT_AVAILABLE,
                    ErrorCode.SMS_NOT_AVAILABLE
            )

    private fun createSmsHeaderInterceptors() =
            ConfigServicePropertySourceLocator.GenericRequestHeaderInterceptor(
                    mapOf(
                            LEAD_APPLICATION to Application.PUZ2.name,
                            ACCEPT to MediaType.APPLICATION_XML_VALUE,
                            CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE
                    )
            )

    internal fun createRequestFactory(readTimeout: Int,
                                      connectTimeout: Int): ClientHttpRequestFactory {
        val requestFactory = HttpComponentsClientHttpRequestFactory(createHttpClient())
        requestFactory.setConnectTimeout(connectTimeout)
        requestFactory.setReadTimeout(readTimeout)
        return requestFactory
    }

    private class ErrorHandlerIgnore : DefaultResponseErrorHandler() {
        @Throws(IOException::class)
        override fun handleError(response: ClientHttpResponse) {
            // do nothing
        }
    }

    private fun createHttpClient(): HttpClient {
        return HttpClientBuilder.create()
                .addInterceptorFirst(LogbookHttpRequestInterceptor(logbook))
                .addInterceptorLast(LogbookHttpResponseInterceptor())
                .setMaxConnPerRoute(maxConnPerRoute)
                .setMaxConnTotal(maxConnTotal)
                .build()
    }
}
