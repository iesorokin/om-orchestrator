package ru.iesorokin.orchestrator.config.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import java.math.BigDecimal


class MoneyBigDecimalDeserializer : JsonDeserializer<BigDecimal>() {

    private val delegate = NumberDeserializers.BigDecimalDeserializer.instance

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext) =
            delegate.deserialize(jsonParser, deserializationContext).setScale(MONEY_SCALE, BigDecimal.ROUND_HALF_UP)
}
