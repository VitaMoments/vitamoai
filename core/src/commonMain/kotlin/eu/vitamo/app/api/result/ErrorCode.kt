package eu.vitamo.app.api.result

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = ErrorCode.Serializer::class)
@JvmInline
value class ErrorCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) {
            "ErrorCode cannot be blank"
        }
    }

    object Serializer : KSerializer<ErrorCode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(
                serialName = "ErrorCode",
                kind = PrimitiveKind.STRING,
            )

        override fun serialize(
            encoder: Encoder,
            value: ErrorCode,
        ) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(
            decoder: Decoder,
        ): ErrorCode {
            return ErrorCode(decoder.decodeString())
        }
    }

    companion object {
        fun http(status: Int): ErrorCode {
            require(status in 100..599) {
                "Invalid HTTP status: $status"
            }

            return ErrorCode("HTTP_$status")
        }
    }

    override fun toString(): String = value
}