package ru.iesorokin.payment.orchestrator.web

import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.web.constants.API_VERSION_V1
import ru.iesorokin.payment.orchestrator.web.constants.EDIT_LINE
import ru.iesorokin.payment.orchestrator.web.constants.START_GIVE_AWAY
import ru.iesorokin.payment.orchestrator.web.converter.DtoConverter
import ru.iesorokin.payment.orchestrator.web.dto.EditLinesRequest
import ru.iesorokin.payment.orchestrator.web.dto.GiveAwayLinesRequest
import javax.validation.Valid

@RestController
@RequestMapping(API_VERSION_V1)
class PaymentTaskController(val paymentTaskService: PaymentTaskService,
                            val dtoConverter: DtoConverter) {

    @ApiOperation("Edit line")
    @PutMapping(EDIT_LINE, produces = [APPLICATION_JSON_VALUE])
    fun editLine(@PathVariable taskId: String,
                 @RequestBody @Valid request: EditLinesRequest) {
        val lines = dtoConverter.convertEditLineRequestToEditLine(request.lines)
        paymentTaskService.editLines(taskId, request.updateBy, lines)
    }

    @ApiOperation("Start give away")
    @PostMapping(START_GIVE_AWAY, produces = [APPLICATION_JSON_VALUE])
    fun startGiveAway(@PathVariable taskId: String,
                      @RequestBody @Valid request: GiveAwayLinesRequest) {
        val giveAwayLines = dtoConverter.convertGiveAwayLinesRequestToGiveAwayLinesInput(request.lines)

        val paymentTask = paymentTaskService.getPaymentTask(taskId)
        val giveAwayId = paymentTaskService.createGiveAway(paymentTask, request.createdBy, giveAwayLines)

        return paymentTaskService.startGiveAwayProcess(giveAwayId, paymentTask)
    }
}