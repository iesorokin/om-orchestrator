package ru.iesorokin.payment.orchestrator.output.client.payment.task.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.domain.AgencyAgreement
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.AddGiveAwayLineRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.GiveAwayResponseItem
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.GiveAwayResponseItemLine
import java.math.BigDecimal
import java.time.LocalDateTime

class GiveAwayConverterTest {

    private val giveAwayConverter = GiveAwayConverter()

    @Test
    fun `toGiveAways - ok`() {
        //Given
        val giveAwayResponses = listOf(giveAwayResponseItem())
        val expected = listOf(giveAway())

        //When
        val actual = giveAwayConverter.toGiveAways(giveAwayResponses)

        //Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should convert giveAwayExternalLine to AddGiveAwayLinesRequest`() {
        val linesFrom = listOf(GiveAwayExternalLine(
                extLineId = "extLineId",
                itemCode = "someCode",
                quantity = BigDecimal.ONE,
                unitAmountIncludingVat = 2.toBigDecimal(),
                agencyAgreement = AgencyAgreement(
                        extLineIds = listOf("extLineId"),
                        supplierName = "sup nam")),
                GiveAwayExternalLine(
                        extLineId = "extLineId_2",
                        itemCode = "someCode_2",
                        quantity = BigDecimal.ONE,
                        unitAmountIncludingVat = 2.toBigDecimal(),
                        agencyAgreement = AgencyAgreement(
                                extLineIds = listOf("extLineId_2"),
                                supplierName = "sup nam second"
                        )))

        val expected = listOf(AddGiveAwayLineRequest(
                extLineId = "extLineId",
                itemCode = "someCode",
                quantity = BigDecimal.ONE,
                unitAmountIncludingVat = 2.toBigDecimal(),
                agencyAgreement = AgencyAgreement(
                        extLineIds = listOf("extLineId"),
                        supplierName = "sup nam")),
                AddGiveAwayLineRequest(
                        extLineId = "extLineId_2",
                        itemCode = "someCode_2",
                        quantity = BigDecimal.ONE,
                        unitAmountIncludingVat = 2.toBigDecimal(),
                        agencyAgreement = AgencyAgreement(
                                extLineIds = listOf("extLineId_2"),
                                supplierName = "sup nam second"
                        )))

        val actual = giveAwayConverter.toGiveAwayLinesRequest(linesFrom)

        Assert.assertEquals(expected, actual)
    }


    private fun giveAway(): GiveAway =
            GiveAway(
                    createdBy = "createdBy",
                    created = LocalDateTime.MIN,
                    processInstanceId = "processInstanceId",
                    lines = listOf(giveAwayExternalLine())
            )

    private fun giveAwayExternalLine(): GiveAwayExternalLine =
            GiveAwayExternalLine(
                    extLineId = "extLineId",
                    itemCode = "itemCode",
                    lineId = "lineId",
                    unitAmountIncludingVat = BigDecimal.ONE,
                    quantity = BigDecimal.TEN
            )

    private fun giveAwayResponseItem(): GiveAwayResponseItem =
            GiveAwayResponseItem(
                    createdBy = "createdBy",
                    created = LocalDateTime.MIN,
                    processInstanceId = "processInstanceId",
                    lines = listOf(giveAwayResponseItemLine())
            )

    private fun giveAwayResponseItemLine(): GiveAwayResponseItemLine =
            GiveAwayResponseItemLine(
                    extLineId = "extLineId",
                    itemCode = "itemCode",
                    lineId = "lineId",
                    unitAmountIncludingVat = BigDecimal.ONE,
                    quantity = BigDecimal.TEN
            )

}