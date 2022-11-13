package com.doordash.interview.phone_number_parser.proxy

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.micronaut.retry.annotation.Retryable
import java.time.Duration
import javax.annotation.PostConstruct
import javax.inject.Singleton
import javax.validation.constraints.Null

interface CacheProxy {
    fun save(value: PhoneNumber, numOccurances: Int):PhoneNumberRecord
}

@Singleton
@Retryable(attempts = "5")
class CacheProxyImpl(
    private val connection: StatefulRedisConnection<String,String>
): CacheProxy {


    @PostConstruct
    fun initialize() {
        connection.sync().auth("password")
    }

    //TODO: Add a username password for redis

//    @Retryable @CircuitBreaker
    override fun save(key: PhoneNumber, numOccurances: Int):PhoneNumberRecord {
        val commands: RedisCommands<String, String> = connection.sync()
        val occurrences = commands.incrby(key.generateKey(), numOccurances.toLong())
        return generatePhoneNumberRecord(key, occurrences.toInt())
    }

    private fun generatePhoneNumberRecord(phoneNumber: PhoneNumber, occurrences: Int): PhoneNumberRecord {
        return PhoneNumberRecord(
            phoneNumber.generateKey(),
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

data class PhoneNumberRecord (
    val id: String,
    val number: String,
    val type: PhoneType,
    val occurrences: Int = 1
)
