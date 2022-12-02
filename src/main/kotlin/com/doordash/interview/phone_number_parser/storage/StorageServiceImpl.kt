package com.doordash.interview.phone_number_parser.storage

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import io.lettuce.core.RedisException
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.slf4j.LoggerFactory
import javax.inject.Singleton

interface StorageService {
    @Throws(StoragePersistException::class)
    fun save(value: PhoneNumber, numOccurrences: Int): PhoneNumberRecord
}

class StoragePersistException(message: String) : Exception(message)

@Singleton
class StorageServiceImpl(
    private val connection: StatefulRedisConnection<String, String>
) : StorageService {

    private val logger = LoggerFactory.getLogger(this::class.java)


    @Throws(StoragePersistException::class)
    override fun save(key: PhoneNumber, numOccurrences: Int): PhoneNumberRecord {
        if (connection.isOpen.not()) {
            throw StoragePersistException("Redis connection is down")
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
            throw StoragePersistException("Error incrementing key due to ${e.localizedMessage}")
        }
    }
}


data class PhoneNumberRecord(
    val id: String,
    val number: String,
    val type: String,
    val occurrences: Int,
)
