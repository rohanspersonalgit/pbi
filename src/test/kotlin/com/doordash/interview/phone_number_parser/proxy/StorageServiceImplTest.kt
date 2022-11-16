package com.doordash.interview.phone_number_parser.proxy

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.lettuce.core.RedisException
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

@MicronautTest
internal class StorageServiceImplTest {

    @Inject
    lateinit var connection: StatefulRedisConnection<String, String>

    @Inject
    lateinit var commands: RedisCommands<String, String>

    private val phoneNumber = PhoneNumber(
        "6048057254",
        PhoneType.HOME
    )

    private val phoneNumberRecord = PhoneNumberRecord(
        phoneNumber.key(),
        phoneNumber.number,
        phoneNumber.phoneType.value,
        1
    )


    @Test
    fun `Save a properly inputted`() {
        val proxy = StorageServiceImpl(connection)
        `when`(connection.isOpen)
            .thenReturn(true)
        `when`(connection.sync())
            .thenReturn(commands)
        `when`(commands.incrby(phoneNumber.key(), 1)).thenReturn(1)
        expectThat(proxy.save(phoneNumber, 1)).isEqualTo(phoneNumberRecord)
    }

    @Test
    fun `throw exception when connection is closed`() {
        val proxy = StorageServiceImpl(connection)
        `when`(connection.isOpen)
            .thenReturn(false)
        expectThrows<StoragePersistException> {
            proxy.save(phoneNumber, 1)
        }
    }

    @Test
    fun `Throw exception when redis command fails`() {
        val proxy = StorageServiceImpl(connection)
        `when`(connection.isOpen)
            .thenReturn(true)
        `when`(connection.sync())
            .thenReturn(commands)
        `when`(commands.incrby(phoneNumber.key(), 1))
            .thenThrow(RedisException::class.java)
        expectThrows<StoragePersistException> {
            proxy.save(phoneNumber, 1)
        }
    }

    @MockBean(RedisCommands::class)
    fun commands(): RedisCommands<String, String> {
        return mock(RedisCommands::class.java) as RedisCommands<String, String>
    }


    @MockBean(StatefulRedisConnection::class)
    fun connection(): StatefulRedisConnection<String, String> {
        return mock(StatefulRedisConnection::class.java) as StatefulRedisConnection<String, String>
    }
}