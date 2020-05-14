package ru.iesorokin.orchestrator.config

import brave.Tracer
import brave.propagation.CurrentTraceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.iesorokin.orchestrator.sleuth.OrchestratorDelegateAspect
import ru.iesorokin.orchestrator.sleuth.SleuthEnum
import ru.iesorokin.utility.sleuthbase.MdcService
import ru.iesorokin.utility.sleuthbase.Slf4jCurrentTraceContext

@Configuration
class SleuthConfig {
    @Bean
    fun mdcService(tracer: Tracer): MdcService {
        return MdcService(tracer)
    }

    @Bean
    fun processDelegateAspect(mdcService: MdcService): OrchestratorDelegateAspect {
        return OrchestratorDelegateAspect(mdcService)
    }

    @Bean
    @Primary
    fun slf4jSpanLogger(): CurrentTraceContext {
        return Slf4jCurrentTraceContext.create(configData = listOf(SleuthEnum.SOLUTION_ID.type, SleuthEnum.PROCESS_ID.type))
    }
}
