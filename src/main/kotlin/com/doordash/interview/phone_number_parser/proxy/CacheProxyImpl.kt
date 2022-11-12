package com.doordash.interview.phone_number_parser.proxy

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import javax.inject.Singleton

interface CacheProxy {
    fun save(value: PhoneNumber):PhoneNumberRecord
}
@Singleton
class CacheProxyImpl(
    // based on micronaut documetation maybe should be using serde TODO
): CacheProxy {


    //TODO: Add a username password for redis

    // this should be injected however I am unfamilliar with micronaut/kotlin DI semantics
    private val connection: StatefulRedisConnection<String, String> = RedisClient.create("redis://localhost:6379").connect()

    override fun save(value: PhoneNumber):PhoneNumberRecord {
        val key = generateKey(value)
        val commands: RedisCommands<String, String> = connection.sync()

        val occurrences = commands.incr(key).toInt()
        return generatePhoneNumberRecord(key, value, occurrences)
    }

    private fun generateKey(value: PhoneNumber): String {
        return value.phoneType.value + value.number
    }

    private fun generatePhoneNumberRecord(id: String, phoneNumber: PhoneNumber, occurrences: Int): PhoneNumberRecord {
        return PhoneNumberRecord(
            id,
            number = phoneNumber.number,
            type = phoneNumber.phoneType,
            occurrences
        )
    }
}

internal data class Record(
    val id: String,
    val occurrences: Int
)

data class PhoneNumberRecord(
    val id: String,
    val number: String,
    val type: PhoneType,
    val occurrences: Int = 1
)
