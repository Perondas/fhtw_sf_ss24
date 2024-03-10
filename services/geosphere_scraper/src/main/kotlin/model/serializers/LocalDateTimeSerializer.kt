package at.fhtw.model.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Array<LocalDateTime>::class)
class ArrayLocalDateTimeSerializer : KSerializer<Array<LocalDateTime>> {
    private val serializer = ArraySerializer(LocalDateTimeSerializer())

    override fun serialize(encoder: Encoder, value: Array<LocalDateTime>) {
        encoder.encodeSerializableValue(serializer, value)

    }

    override fun deserialize(decoder: Decoder): Array<LocalDateTime> {
        return decoder.decodeSerializableValue(serializer)
    }
}