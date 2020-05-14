package ru.iesorokin.orchestrator.config.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.math.BigDecimal

class TaxBigDecimalSerializer : JsonSerializer<BigDecimal>() {

    override fun serialize(value: BigDecimal, gen: JsonGenerator, serializers: SerializerProvider) =
            gen.writeNumber(value.setScale(TAX_SCALE, BigDecimal.ROUND_HALF_UP))
}
