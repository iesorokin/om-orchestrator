package ru.iesorokin.orchestrator.camunda.bpmn

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.util.ClockUtil
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

abstract class BaseCamundaTest {

    protected lateinit var processInstance: ProcessInstance

    companion object {
        @Rule
        @ClassRule
        @JvmStatic
        lateinit var rule: ProcessEngineRule
    }

    @Before
    fun prepareAbstractAssertions() {
        AbstractAssertions.init(rule.processEngine)
    }

    @After
    fun tearDownRule() {
        val processList = rule.runtimeService.createExecutionQuery().active().list()
        assertThat(processList).isEmpty()
    }

    protected fun getActiveSubscriptions(processInstanceId: String): List<String> =
            rule.runtimeService.createEventSubscriptionQuery()
                    .processInstanceId(processInstanceId)
                    .eventType("message")
                    .list()
                    .map { it.eventName }

    protected fun assertJobRetry(retryCount: Int,
                                 retryMinutes: Int) {
        var retryCounter = retryCount
        while (--retryCounter >= 0) {
            val retries = executeJob(processInstance.processInstanceId)
            Assertions.assertThat(retries).isEqualTo(retryCounter)
            if (retries != 0) {
                assertLockExpirationTime(retryMinutes)
            }
        }
    }

    protected fun skipTimer(processInstanceId: String) {
        val job = rule.managementService.createJobQuery().processInstanceId(processInstanceId).active().singleResult()
        rule.managementService.executeJob(job.id)
    }

    protected fun executeJob(processInstanceId: String, activityId: String? = null): Int? {
        val job = rule.managementService.createJobQuery().processInstanceId(processInstanceId)
                .let {
                    if (activityId == null) {
                        it
                    }
                    else {
                        it.activityId(activityId)
                    }
                }.singleResult()
        try {
            rule.managementService.executeJob(job.id)
        } catch (ignored: Exception) {
        }

        return rule.managementService.createJobQuery().processInstanceId(processInstanceId).singleResult()?.retries
    }

    protected fun assertLockExpirationTime(minutesOffset: Int) {
        val availableTestLagInSeconds = 5L
        val currentTime = ClockUtil.getCurrentTime().toLocalDate()
        val addMinutes = currentTime.plusMinutes(minutesOffset.toLong())
        val lockExpirationTime = (rule.managementService.createJobQuery().singleResult() as JobEntity)
                .lockExpirationTime.toLocalDate()
        AbstractAssertions.assertThat(ChronoUnit.SECONDS.between(lockExpirationTime, addMinutes)).isBetween(-availableTestLagInSeconds, availableTestLagInSeconds)
        ClockUtil.setCurrentTime(addMinutes.toDate())
    }
}

internal fun Date.toLocalDate() = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withNano(0)
internal fun LocalDateTime.toDate() = Date.from(atZone(ZoneId.systemDefault()).toInstant())


internal fun ProcessEngineRule.endProcess(processInstance: ProcessInstance) {
    try {
        runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    } catch (ignored: ProcessEngineException) {
    } finally {
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}
