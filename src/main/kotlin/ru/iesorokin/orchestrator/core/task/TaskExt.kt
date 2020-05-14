package ru.iesorokin.payment.orchestrator.core.task

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.value.IntegerValue
import org.camunda.bpm.engine.variable.value.LongValue
import org.camunda.bpm.engine.variable.value.StringValue
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BpmnErrors
import ru.iesorokin.payment.orchestrator.core.exception.CamundaProcessNotCorrectStateException

internal fun DelegateExecution.variable(variableName: String,
                                        errorMsg: (() -> String)? = null) =
        this.getVariableTyped<StringValue>(variableName)?.value
                ?: throw CamundaProcessNotCorrectStateException(errorMsg?.invoke() ?: "Not found $variableName")

internal fun DelegateExecution.variableOrNull(variableName: String) =
        this.getVariableTyped<StringValue>(variableName)?.value

internal fun DelegateExecution.intVariable(variableName: String,
                                        errorMsg: (() -> String)? = null) =
        this.getVariableTyped<IntegerValue>(variableName)?.value
                ?: throw CamundaProcessNotCorrectStateException(errorMsg?.invoke() ?: "Not found $variableName")

internal fun DelegateExecution.variableLocal(variableName: String,
                                             errorMsg: (() -> String)? = null) =
        this.getVariableLocalTyped<StringValue>(variableName)?.value
                ?: throw CamundaProcessNotCorrectStateException(errorMsg?.invoke() ?: "Not found $variableName")

internal fun DelegateExecution.longVariable(variableName: String,
                                            errorMsg: (() -> String)? = null) =
        this.getVariableTyped<LongValue>(variableName)?.value
                ?: throw CamundaProcessNotCorrectStateException(errorMsg?.invoke() ?: "Not found $variableName")

internal fun DelegateExecution.paymentTaskId() =
        this.getVariableTyped<StringValue>(PAYMENT_TASK_ID)?.value ?: throw BpmnErrors.PAYMENT_NOT_FOUND.toBpmnError()

internal fun DelegateExecution?.incrementRetryCounter(counterName: String) {
    var retryCount = this?.getVariable(counterName) as Int? ?: 0
    this?.setVariable(counterName, ++retryCount)
}

internal fun DelegateExecution.createRetryCounter(counterName: String) {
    if (this.getVariable(counterName) as Int? ?: 0 == 0) this.setVariable(counterName, 0)
}
