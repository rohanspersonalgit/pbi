package com.doordash.interview.phone_number_parser

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import javax.inject.Inject
import kotlin.random.Random

@Testcontainers
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseEndToEndTest(sharedRedis: Boolean) : TestPropertyProvider {
    private val REDIS_PORT = 6379

    private val REDIS_IMAGE_NAME = "redis:7.0.5"

    private val RESPONSE_STRING_FORMAT = "{\"id\":\"%s\",\"number\":\"%s\",\"type\":\"%s\",\"occurrences\":%d}"

    internal val RESPONSE_ARRAY_TEMPLATE = "[%s]"

    @Inject
    @field:Client("/")
    lateinit var client: RxHttpClient

    private var redis: GenericContainer<KGenericContainer> = GenericContainer<KGenericContainer>(
        DockerImageName.parse(REDIS_IMAGE_NAME)
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
        withReuse(sharedRedis)
        start()
    }

    override fun getProperties(): MutableMap<String, String> {
        return mutableMapOf(
            "redis.host" to redis.host,
            "redis.port" to redis.firstMappedPort.toString(),
            "redis.password" to ""
        )
    }

    fun generateRequestJson(requestString: String): String {
        return "{\"raw_phone_numbers\": \"%s\"}".format(requestString)
    }

    fun isEven(): Boolean {
        return Random.nextInt() % 2 == 0
    }

    fun generateBadRequestString(numTimes: Int): String {
        val invalidChar = "invalidChar"
        val badString: String = if (Random.nextInt() % 2 == 0) {
            "(cell)" + (1..9).map {
                if (isEven()) Random.nextInt(0,9) else invalidChar
            }
                .joinToString("") + invalidChar
        } else {
            "(Cell)" + (1..Random.nextInt(5,15)).map {
                Random.nextInt(0,9)
            }
                .joinToString("")
        }
        return badString.repeat(numTimes)
    }

    fun generateResponseString(phoneNumberRecord: PhoneNumber, occurances: Int): String {
        return RESPONSE_STRING_FORMAT
            .format(
                phoneNumberRecord.key(),
                phoneNumberRecord.number,
                phoneNumberRecord.phoneType.value,
                occurances
            )
    }

    fun generateRandomPhoneNumber(): PhoneNumber {
        val randoNum = (1..10).map {
            Random.nextInt(0, 9)
        }
        val cellPhoneType = if (Random.nextInt() % 2 == 0) {
            PhoneType.CELL
        } else {
            PhoneType.HOME
        }
        return PhoneNumber(
            randoNum.joinToString(""),
            cellPhoneType
        )
    }

    fun generateRandomPhoneNumbers(inputMap: MutableMap<PhoneNumber,Int>, repeatTimes: Int) : List<PhoneNumber> {
        return (1..Random.nextInt(1, repeatTimes)).map {
            val generatedNumber = generateRandomPhoneNumber()
            inputMap[generatedNumber] = inputMap.getValue(generatedNumber) + 1
            generatedNumber
        }
    }

    fun generatePhoneType(type: PhoneType): String{
        return when(type) {
            PhoneType.CELL -> ("(Cell)")
            PhoneType.HOME -> ("(Home)")
            else -> fail("Error with phone type")
        }
    }

    fun generateRequestString(phoneNumbers: List<PhoneNumber>): String {
        return phoneNumbers.joinToString(",") { phoneNumber ->
            val phoneTypeString = generatePhoneType(phoneNumber.phoneType)

            if (Random.nextInt() % 2 == 0) {
                phoneTypeString + phoneNumber.number
            } else {
                val numberString = phoneNumber.number.map {
                    if (Random.nextInt() % 2 == 0) {
                        it
                    } else {
                        it + "s"
                    }
                }
                phoneTypeString + numberString.joinToString("")
            }
        }
    }
}

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)