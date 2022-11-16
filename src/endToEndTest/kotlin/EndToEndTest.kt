package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Duration
import javax.inject.Inject
import kotlin.random.Random


@Testcontainers
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Test : TestPropertyProvider {
    private val REDIS_PORT = 6379

    @Inject
    @field:Client("/")
    lateinit var client: RxHttpClient


    private var redis: GenericContainer<KGenericContainer> = GenericContainer<KGenericContainer>(
        DockerImageName.parse("redis:7.0.5")
    ).apply {
        withExposedPorts(REDIS_PORT)
        waitingFor(
            Wait.forLogMessage(".*Ready to accept connections.*\\n", 1)
        )
        withStartupTimeout(Duration.ofSeconds(2))
        withLogConsumer(
            Slf4jLogConsumer(
                LoggerFactory
                    .getLogger(KGenericContainer::class.java)
            )
        )
        withReuse(false)
        start()
    }


    @Test
    fun `properly handle a single input`() {
        val number = generateRandomPhoneNumber()
        val requestString = generateRequestString(number)
        println(requestString)
        val responseString = "[" + generateResponseString(number, 1) + "]"

        val resp = client.toBlocking().retrieve(
            HttpRequest.POST(
                "/phone-numbers",
                requestString
            )
        )
        expectThat(resp)
            .isEqualTo(responseString)
    }


    fun generateResponseString(phoneNumberRecord: PhoneNumber, occurances: Int): String {
        return "{\"id\":\"%s\",\"number\":\"%s\",\"type\":\"%s\",\"occurrences\":%d}"
            .format(phoneNumberRecord.key(), phoneNumberRecord.number, phoneNumberRecord.phoneType.value, occurances)
    }

    private fun generateRandomPhoneNumber(): PhoneNumber {
        val randoNum = (1..10).map {
            Random.nextInt(0, 9)
        }
        val cellPhoneType = if (Random.nextInt() % 2 == 0) PhoneType.CELL else PhoneType.HOME
        return PhoneNumber(randoNum.joinToString(""), cellPhoneType)
    }

    fun generateRequestString(phoneNumber: PhoneNumber): String {
        val template = "{\"raw_phone_numbers\": \"%s\"}"
        val phoneTypeString = "(" + phoneNumber.phoneType.value + ")"
        return if (Random.nextInt() % 2 == 0) {
            String.format(template, phoneTypeString + phoneNumber.number)
        } else {
            val numberString = phoneNumber.number.map {
                if (Random.nextInt() % 2 == 0) {
                    it
                } else {
                    it + "s"
                }
            }
            String.format(template, phoneTypeString + numberString.joinToString(""))
        }
    }
//
//    @Test
//    fun `testing 2`(){
//        println(resp)
//    }

    override fun getProperties(): MutableMap<String, String> {
        return mutableMapOf(
            "redis.host" to redis.host,
            "redis.port" to redis.firstMappedPort.toString(),
            "redis.password" to ""
        )
    }
}
