package com.doordash.interview.phone_number_parser

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import io.micronaut.http.HttpRequest

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random


@Testcontainers
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EndToEndTest : BaseEndToEndTest(false) {

    @Test
    fun `properly handle a single input`() {
        val number = generateRandomPhoneNumber()
        val requestString = generateRequestString(listOf(number))
        val responseString = RESPONSE_ARRAY_TEMPLATE
            .format(
                generateResponseString(number,1)
            )

        val resp = executeRequest(requestString)
        expectThat(resp)
            .isEqualTo(responseString)
    }

    @Test
    fun `try up to 10 random phone numbers and validate the response`() {
        val inputMap = mutableMapOf<PhoneNumber, Int>().withDefault { 0 }
        val numbers = generateRandomPhoneNumbers(inputMap, Random.nextInt(2, 10))

        val resp = executeRequest(
            generateRequestString(numbers)
        )
        val responseString = inputMap.entries.map {
            generateResponseString(it.key, it.value)
        }
        expectThat(resp)
            .isEqualTo(RESPONSE_ARRAY_TEMPLATE
                .format(
                    responseString.joinToString(",")
                )
            )
    }

    @Test
    fun `try the same inputs twice and ensure it reflects the proper occurances`() {
        val inputMap = mutableMapOf<PhoneNumber, Int>().withDefault { 0 }
        val numbers = generateRandomPhoneNumbers(inputMap, Random.nextInt(2, 10))

        val resp = executeRequest(
            generateRequestString(numbers)
        )
        val responseString = inputMap.entries.map {
            generateResponseString(it.key, it.value)
        }
        expectThat(resp)
            .isEqualTo(RESPONSE_ARRAY_TEMPLATE
                .format(
                    responseString.joinToString(",")
                )
            )
        val resp2 = executeRequest(
            generateRequestString(numbers)
        )

        val responseString2 = inputMap.entries.map {
            generateResponseString(it.key, it.value *2)
        }

        expectThat(resp2)
            .isEqualTo(RESPONSE_ARRAY_TEMPLATE
                .format(
                    responseString2.joinToString(",")
                )
            )

    }

    @Test
    fun `try a mix of good and bad inputs`() {
        val inputMap = mutableMapOf<PhoneNumber, Int>()
            .withDefault { 0 }

        val numbers = generateRandomPhoneNumbers(inputMap, 2)

        val badRequestString = generateBadRequestString(2)

        val moreNumbers = generateRandomPhoneNumbers(inputMap, 2)

        val anotherBadString = generateBadRequestString(2)

        val finalString = if (isEven()) {
            generateBadRequestString(2)
        } else {
            val finalRandNumbers = generateRandomPhoneNumbers(inputMap, 2)
            generateRequestString(finalRandNumbers)
        }

        val resp = executeRequest(
            generateRequestString(numbers) +
                    badRequestString +
                    generateRequestString(moreNumbers) +
                    anotherBadString +
                    finalString
        )

        val responseString = inputMap.entries.map { generateResponseString(it.key, it.value) }

        expectThat(resp)
            .isEqualTo(RESPONSE_ARRAY_TEMPLATE
                .format(
                    responseString.joinToString(",")
                )
            )
    }

    private fun executeRequest(requestString: String): String? {
        return client.toBlocking().retrieve(
            HttpRequest.POST(
                "/phone-numbers",
                generateRequestJson(requestString)
            )
        )
    }
}

