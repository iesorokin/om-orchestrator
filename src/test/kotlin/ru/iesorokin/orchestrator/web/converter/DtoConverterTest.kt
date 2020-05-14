package ru.iesorokin.payment.orchestrator.web.converter

import org.assertj.core.api.Assertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.web.dto.EditLineRequest

class DtoConverterTest{

    private val dtoConverter = DtoConverter()

    @Test
    fun `convertEditLineRequestToEditLine should correct convert EditLineRequest to EditLines`() {
        val extLineIdOne = "extLineIdOne"
        val extLineIdTwo = "extLineIdTwo"
        val vat = 1.toBigDecimal()
        val quantity = 2.toBigDecimal()

        val request = listOf(
                EditLineRequest(extLineId = extLineIdOne,unitAmountIncludingVat = vat),
                EditLineRequest(extLineId = extLineIdTwo,confirmedQuantity = quantity)
        )

        val expected = listOf(
                EditLine(extLineId = extLineIdOne,unitAmountIncludingVat = vat),
                EditLine(extLineId = extLineIdTwo,confirmedQuantity = quantity)
        )

        val actual = dtoConverter.convertEditLineRequestToEditLine(request)
        Assertions.assertThat(actual).isEqualTo(expected)
    }
}