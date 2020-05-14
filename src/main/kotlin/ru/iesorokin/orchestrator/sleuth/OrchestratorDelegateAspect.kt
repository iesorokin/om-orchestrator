package ru.iesorokin.payment.orchestrator.sleuth

import mu.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.camunda.bpm.engine.delegate.DelegateExecution
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.task.variable
import ru.iesorokin.payment.orchestrator.sleuth.SleuthEnum.PROCESS_ID
import ru.iesorokin.payment.orchestrator.sleuth.SleuthEnum.SOLUTION_ID
import ru.iesorokin.utility.sleuthbase.MdcService

/**
 * Aspect intercepts methods with annotation [ExtractProcessDataFromDelegate] and propagates solution id and process instance id in MDC
 * To propagate params over child spans one should add field name in list under spring.sleuth.baggage-keys
 * Example:+
 * spring:
 *      sleuth:
 *          baggage-keys:
 *              - solutionId
 *              - processId
 */

private val log = KotlinLogging.logger {}

@Aspect
open class OrchestratorDelegateAspect(private val mdcService: MdcService) {

    @Pointcut(value = "execution(public * ru.iesorokin.payment..*(..))")
    fun anyLmPaymentPublicMethod() {
    }

    @Before("anyLmPaymentPublicMethod() && @annotation(extractProcessDataFromDelegate)")
    fun putProcessDataInMdc(joinPoint: JoinPoint, extractProcessDataFromDelegate: ExtractProcessDataFromDelegate) {
        var processId: String? = null
        try {
            val execution = joinPoint.args.first { it is DelegateExecution } as DelegateExecution

            processId = execution.processInstanceId
            mdcService.propagateMdc(PROCESS_ID.type, processId)
            val solutionId = execution.variable(EXT_ORDER_ID)
            mdcService.propagateMdc(SOLUTION_ID.type, solutionId)
        } catch (e: Exception) {
            log.error { "Error when extracting data from delegate with processId: $processId" }
        }
    }
}
