package ru.iesorokin.orchestrator.web

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.iesorokin.orchestrator.core.domain.EditLine
import ru.iesorokin.orchestrator.core.domain.GiveAwayLine
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.exception.InvalidLineTypeException
import ru.iesorokin.orchestrator.core.exception.InvalidTaskStatusException
import ru.iesorokin.orchestrator.core.exception.InvalidUnitAmountIncludingVatException
import ru.iesorokin.orchestrator.core.exception.LineNotFoundException
import ru.iesorokin.orchestrator.core.exception.PaymentClientException
import ru.iesorokin.orchestrator.core.exception.PaymentTaskNotFoundException
import ru.iesorokin.orchestrator.core.exception.StartProcessException
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.web.constants.API_VERSION_V1
import ru.iesorokin.orchestrator.web.constants.EDIT_LINE
import ru.iesorokin.orchestrator.web.constants.START_GIVE_AWAY

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@AutoConfigureMockMvc
@DirtiesContext
class PaymentTaskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @SpyBean
    private lateinit var paymentTaskService: PaymentTaskService

    private lateinit var paymentTask: PaymentTask
    private val taskId = "taskId"

    @Before
    fun setup() {
        paymentTask = createPaymentTask(taskId)
        doReturn(paymentTask).whenever(paymentTaskService).getPaymentTask(taskId)
    }

    @Test
    fun `put edit-line should return ok`() {
        val taskId = "taskId"
        doNothing().whenever(paymentTaskService).editLines(taskId,"system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isOk)
                .andExpect(content().string(""))

        verify(paymentTaskService, times(1)).editLines(taskId,"system2", requestForEditLine)
    }

    @Test
    fun `put edit-line should return error bad request and error code = 100 when service throw PaymentTaskNotFoundException`() {
        val taskId = "taskId"
        doThrow(PaymentTaskNotFoundException("")).whenever(paymentTaskService).editLines(taskId, "system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isBadRequest)
                .andExpect(content().json(getErrorJson(100, "Task not found.")))
    }

    @Test
    fun `put edit-line should return error bad request and error code = 101 when service throw IncorrectLineTypeException`() {
        val taskId = "taskId"
        doThrow(InvalidLineTypeException("")).whenever(paymentTaskService).editLines(taskId,"system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isBadRequest)
                .andExpect(content().json(getErrorJson(101, "Editing of unitAmountIncludingVat is available only for delivery lines.")))
    }

    @Test
    fun `put edit-line should return error bad request and error code = 102 when service throw IncorrectUnitAmountIncludingVatException`() {
        val taskId = "taskId"
        doThrow(InvalidUnitAmountIncludingVatException("")).whenever(paymentTaskService).editLines(taskId,"system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isBadRequest)
                .andExpect(content().json(getErrorJson(102, "'unitAmountIncludingVat' must be less or equal than current value and not negative.")))
    }

    @Test
    fun `put edit-line should return error bad request and error code = 103 when service throw IncorrectTaskStatusException`() {
        val taskId = "taskId"
        doThrow(InvalidTaskStatusException("")).whenever(paymentTaskService).editLines(taskId,"system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isBadRequest)
                .andExpect(content().json(getErrorJson(103, "Editing of 'unitAmountIncludingVat' " +
                        "is available only for SBERLINK_WITH_TPNET_DEPOSIT with payment status HOLD or " +
                    "for POD_AGENT/POD_POST_PAYMENT with payment status CONFIRMED.")))
    }

    @Test
    fun `put edit-line should return error is conflict and error code = 104 when service throw LineNotFoundException`() {
        val taskId = "taskId"
        doThrow(LineNotFoundException("")).whenever(paymentTaskService).editLines(taskId,"system2", requestForEditLine)

        mockMvc.perform(put("/$API_VERSION_V1$EDIT_LINE", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForEditLineJson))
                .andExpect(status().isConflict)
                .andExpect(content().json(getErrorJson(104, "Input extLineId does not exist in payment task.")))
    }

    @Test
    fun `post start-give-away should return ok`() {
        val giveAwayId = "giveAwayId"

        doReturn(giveAwayId).whenever(paymentTaskService).createGiveAway(paymentTask, "system2", requestForGiveAway)
        doNothing().whenever(paymentTaskService).startGiveAwayProcess(giveAwayId, paymentTask)

        mockMvc.perform(post("/$API_VERSION_V1$START_GIVE_AWAY", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForGiveAwayJson))
                .andExpect(status().isOk)
                .andExpect(content().string(""))

        verify(paymentTaskService).getPaymentTask(taskId)
        verify(paymentTaskService).createGiveAway(paymentTask,"system2", requestForGiveAway)
        verify(paymentTaskService).startGiveAwayProcess(giveAwayId, paymentTask)
    }

    @Test
    fun `post start-give-away should return error bad request and error code = 100 when service throw PaymentTaskNotFoundException`() {
        doThrow(PaymentTaskNotFoundException("")).whenever(paymentTaskService).createGiveAway(paymentTask,"system2", requestForGiveAway)

        mockMvc.perform(post("/$API_VERSION_V1$START_GIVE_AWAY", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForGiveAwayJson))
                .andExpect(status().isBadRequest)
                .andExpect(content().json(getErrorJson(100, "Task not found.")))

        verify(paymentTaskService, never()).startGiveAwayProcess(any(), any())
    }

    @Test
    fun `post start-give-away should return error is conflict and error code = 104 when service throw LineNotFoundException`() {
        doThrow(LineNotFoundException("")).whenever(paymentTaskService).createGiveAway(paymentTask,"system2", requestForGiveAway)

        mockMvc.perform(post("/$API_VERSION_V1$START_GIVE_AWAY", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForGiveAwayJson))
                .andExpect(status().isConflict)
                .andExpect(content().json(getErrorJson(104, "Input extLineId does not exist in payment task.")))

        verify(paymentTaskService, never()).startGiveAwayProcess(any(), any())
    }

    @Test
    fun `post start-give-away should return Internal Server Error and error code = 107 when service throw StartProcessException`() {
        val giveAwayId = "id"

        doReturn(giveAwayId).whenever(paymentTaskService).createGiveAway(paymentTask, "system2", requestForGiveAway)
        doThrow(StartProcessException("")).whenever(paymentTaskService).startGiveAwayProcess(any(),any())

        mockMvc.perform(post("/$API_VERSION_V1$START_GIVE_AWAY", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForGiveAwayJson))
                .andExpect(status().isInternalServerError)
                .andExpect(content().json(getErrorJson(107, "Failed to start process.")))

        verify(paymentTaskService).createGiveAway(paymentTask, "system2", requestForGiveAway)
    }

    @Test
    fun `post start-give-away should return Internal Server Error and error code = 107 when service throw PaymentClientException`() {
        doThrow(PaymentClientException("")).whenever(paymentTaskService).createGiveAway(paymentTask, "system2", requestForGiveAway)

        mockMvc.perform(post("/$API_VERSION_V1$START_GIVE_AWAY", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestForGiveAwayJson))
                .andExpect(status().isInternalServerError)
                .andExpect(content().json(getErrorJson(105, "Payment task is not available.")))

        verify(paymentTaskService, never()).startGiveAwayProcess(any(), any())
    }

    private fun getErrorJson(code: Int, message: String) =
                        """{
                          "errors": [
                            {
                              "code": $code,
                              "message": "$message"
                            }
                          ]
                        }""".trimIndent()

    private val requestForEditLine = listOf(
            EditLine(extLineId = "extLineIdOne",unitAmountIncludingVat = 1.toBigDecimal()),
            EditLine(extLineId = "extLineIdTwo",unitAmountIncludingVat = 2.toBigDecimal()),
            EditLine(extLineId = "extLineIdThree",unitAmountIncludingVat = 3.toBigDecimal())
    )

    @Language("JSON")
    private val requestForEditLineJson = """
            {
               "updateBy":"system2",
               "lines":[
                  {
                     "extLineId":"extLineIdOne",
                     "unitAmountIncludingVat":1
                  },
                  {
                     "extLineId":"extLineIdTwo",
                     "unitAmountIncludingVat":2
                  },
                  {
                     "extLineId":"extLineIdThree",
                     "unitAmountIncludingVat":3
                  }
               ]
            }
        """.trimIndent()

    private val requestForGiveAway = listOf(
            GiveAwayLine(extLineId = "extLineIdOne", quantity = 1.toBigDecimal()),
            GiveAwayLine(extLineId = "extLineIdTwo", quantity = 2.toBigDecimal()),
            GiveAwayLine(extLineId = "extLineIdThree", quantity = 3.toBigDecimal())
    )

    private fun createPaymentTask(taskId: String) = PaymentTask(
            taskId = taskId,
            taskStatus = "HOLD",
            taskType = "SOLUTION",
            extOrderId = "extOrderId",
            executionStore = 12,
            lines = emptyList())

    @Language("JSON")
    private val requestForGiveAwayJson = """
            {
               "createdBy":"system2",
               "lines":[
                  {
                     "extLineId":"extLineIdOne",
                     "quantity":1
                  },
                  {
                     "extLineId":"extLineIdTwo",
                     "quantity":2
                  },
                  {
                     "extLineId":"extLineIdThree",
                     "quantity":3
                  }
               ]
            }
        """.trimIndent()

}
