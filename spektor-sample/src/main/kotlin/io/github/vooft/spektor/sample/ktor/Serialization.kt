package io.github.vooft.spektor.sample.ktor

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializer
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.reflect.KClass

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = SerializersModule {
                    contextual(
                        kClass = Instant::class,
                        serializer = object : KSerializer<Instant> {
                            override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
                            override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
                            override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
                        }
                    )
                    contextual(
                        kClass = LocalDate::class,
                        serializer = object : KSerializer<LocalDate> {
                            override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
                            override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
                            override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
                        }
                    )
                    contextual(
                        kClass = UUID::class,
                        serializer = object : KSerializer<UUID> {
                            override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
                            override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
                            override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
                        }
                    )
                    contextual(
                        kClass = URI::class,
                        serializer = object : KSerializer<URI> {
                            override val descriptor = PrimitiveSerialDescriptor("URI", STRING)
                            override fun deserialize(decoder: Decoder): URI = URI(decoder.decodeString())
                            override fun serialize(encoder: Encoder, value: URI) = encoder.encodeString(value.toString())
                        }
                    )
                }
            }
        )
    }
}
