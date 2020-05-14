package ru.iesorokin.payment.orchestrator.bpmn.test

import org.camunda.bpm.engine.test.ProcessEngineRule
import org.junit.ClassRule
import org.junit.runners.model.Statement
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

class CamundaCoverageSpringJUnit4ClassRunner(clazz: Class<*>) : SpringJUnit4ClassRunner(clazz) {
    override fun withBeforeClasses(s: Statement): Statement {
        testClass.getAnnotatedFields(ClassRule::class.java).forEach {
            when (it.type) {
                ProcessEngineRule::class.java -> {
                    it.field.set(
                            it,
                            this.testContextManager.testContext.applicationContext.getBean(ProcessEngineRule::class.java)
                    )
                }
            }
        }
        return super.withBeforeClasses(s)
    }
}
