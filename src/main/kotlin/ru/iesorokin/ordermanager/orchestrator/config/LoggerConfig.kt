package ru.iesorokin.ordermanager.orchestrator.config

import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfig {
    @Bean
    fun logger(): KLogger = KotlinLogging.logger {}
}