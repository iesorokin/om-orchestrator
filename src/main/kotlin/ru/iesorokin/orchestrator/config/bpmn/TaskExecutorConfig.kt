package ru.iesorokin.payment.orchestrator.config.bpmn


import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

private const val CAMUNDA_TASK_EXECUTOR = "camundaTaskExecutor"

@RefreshScope
@Configuration
class TaskExecutorConfig(@Value("\${taskExecutor.corePoolSize}")
                         private val corePoolSize: Int,
                         @Value("\${taskExecutor.maxPoolSize}")
                         private val maxPoolSize: Int) {

    @Bean(name = [CAMUNDA_TASK_EXECUTOR])
    @ConditionalOnProperty(prefix = "camunda.bpm.job-execution", name = ["enabled"],
            havingValue = "true", matchIfMissing = true)
    fun camundaTaskExecutor(properties: CamundaBpmProperties): TaskExecutor =
            ThreadPoolTaskExecutor().also {
                it.corePoolSize = corePoolSize
                it.maxPoolSize = maxPoolSize
            }
}
