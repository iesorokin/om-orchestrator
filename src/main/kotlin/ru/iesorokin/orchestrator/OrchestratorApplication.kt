package ru.iesorokin.payment.orchestrator

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.stream.annotation.EnableBinding
import ru.iesorokin.payment.orchestrator.config.MessagingSource

@SpringBootApplication(exclude = [RabbitAutoConfiguration::class])
@EnableBinding(MessagingSource::class)
@EnableEurekaClient
@EnableProcessApplication
class OrchestratorApplication

fun main(args: Array<String>) {
    SpringApplication.run(OrchestratorApplication::class.java, *args)
}
