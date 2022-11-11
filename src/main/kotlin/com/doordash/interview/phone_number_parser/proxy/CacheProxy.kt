package com.doordash.interview.phone_number_parser.proxy

import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.CompressionCodec
import io.lettuce.core.codec.RedisCodec
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.Serdeable
import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.Charset


class CacheProxy(
//    private val objectMapper: ObjectMapper
) {

    //TODO: Add a username password for redis

    // this should be injected however I am unfamilliar with micronaut/kotlin DI semantics
    private val connection: RedisClient = RedisClient.create("redis://localhost:6379")

    fun save() {
//        val commands: RedisCommands<String, String> = connection.connect().sync()
//        commands.set("test", objectMapper.writeValueAsString(PhoneNumberRecord(
//            "",
//            "605",
//            PhoneType.CELL,
//            1
//        )))
//
//        val res = commands.get("test")
//        println(objectMapper.readValue(res, PhoneNumberRecord::class.java))
    }
}

@Serdeable
data class PhoneNumberRecord(
    val id: String,
    val number: String,
    val type: PhoneType,
    val occurrences: Int
)

class SerializedObjectCodec : RedisCodec<String, Any> {
    private val charset = Charset.forName("UTF-8")
    override fun decodeKey(bytes: ByteBuffer): String {
        return charset.decode(bytes).toString()
    }

    override fun decodeValue(bytes: ByteBuffer): Any {
        return try {
            val array = ByteArray(bytes.remaining())
            bytes[array]
            val `is` = ObjectInputStream(ByteArrayInputStream(array))
            `is`.readObject()
        } catch (e: Exception) {
            throw e
        }
    }

    override fun encodeKey(key: String): ByteBuffer {
        return charset.encode(key)
    }

    override fun encodeValue(value: Any): ByteBuffer {
        return try {
            val bytes = ByteArrayOutputStream()
            val os = ObjectOutputStream(bytes)
            os.writeObject(value)
            ByteBuffer.wrap(bytes.toByteArray())
        } catch (e: IOException) {
            ByteBuffer.wrap(ByteArray(0))
            throw e
        }
    }
}