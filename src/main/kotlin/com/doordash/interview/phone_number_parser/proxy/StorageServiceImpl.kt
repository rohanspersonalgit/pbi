package com.doordash.interview.phone_number_parser.proxy

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.lettuce.core.RedisException
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.retry.annotation.Retryable
import org.slf4j.LoggerFactory
import javax.inject.Singleton

interface StorageService {
    @Throws(StoragePersistException::class)
    fun save(value: PhoneNumber, numOccurrences: Int): PhoneNumberRecord
}

class StoragePersistException : Exception("Redis is down")

@Singleton
@Context
@Retryable(attempts = "5")
class StorageServiceImpl(
    private val connection: StatefulRedisConnection<String, String>
) : StorageService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    //TODO: Add a username password for redis

    //    @Retryable @CircuitBreaker

    // TODO even if we fail or osmethign how long do we want to wait to keep retyring and eventually give up
    @Throws(StoragePersistException::class)
    override fun save(key: PhoneNumber, numOccurrences: Int): PhoneNumberRecord {
        if (connection.isOpen.not()) {
            throw StoragePersistException()
        }
        try {
            val commands: RedisCommands<String, String> = connection.sync()
            val occurrences = commands.incrby(key.key(), numOccurrences.toLong())
            logger.info("updated ${key.key()} counter to $occurrences")
            return PhoneNumberRecord(
                key.key(),
                key.number,
                key.phoneType.value,
                occurrences.toInt()
            )
        } catch (e: RedisException) {
            logger.error(
                "Unable to increment ${key.key()} by $numOccurrences" +
                        "due to ${e.cause}"
            )
            throw StoragePersistException()
        }
    }
}


data class PhoneNumberRecord(
    val id: String,
    val number: String,
    val type: String,
    val occurrences: Int,
)
