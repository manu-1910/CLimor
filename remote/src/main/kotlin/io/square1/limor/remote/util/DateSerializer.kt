package io.square1.limor.remote.util

import io.square1.limor.remote.services.DATE_FORMAT_MASK
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    private val df: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_MASK)

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WithCustomDefault", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, obj: Date) {
        encoder.encodeString(df.format(obj))
    }

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())
    }
}