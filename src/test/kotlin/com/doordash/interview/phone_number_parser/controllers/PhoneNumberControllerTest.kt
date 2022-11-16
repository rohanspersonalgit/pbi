package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.Parser
import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.doordash.interview.phone_number_parser.proxy.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.*
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.testcontainers.containers.GenericContainer
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import javax.inject.Inject


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhoneNumberControllerTest {

    @Inject
    lateinit var cacheProxy: StorageService

    @field:Client("/")
    @Inject
    lateinit var client: RxHttpClient

    private val phoneNumber = PhoneNumber(
        "6048357354",
        PhoneType.HOME
    )

    @BeforeAll
    internal fun beforeAll() {
        mockkObject(Parser)
    }

    @AfterEach
    internal fun afterEach() {
       reset(cacheProxy)
        clearMocks(Parser)
    }

    private val phoneNumberRecord = PhoneNumberRecord(
        phoneNumber.key(),
        phoneNumber.number,
        phoneNumber.phoneType.value,
        1
    )

    private val jsonString = "{\"raw_phone_numbers\": \"(Home) 6048357354\"}"


    @Test
    fun `Return 200 for a single phone number`() {
        `when`(cacheProxy.save(phoneNumber, 1))
            .thenReturn(phoneNumberRecord)

        every {Parser.clean("(Home) 6048357354")} returns mapOf(phoneNumber to 1)
        val call = client.exchange(
            HttpRequest.POST(
                "/phone-numbers",
                jsonString
            ),
            PhoneNumberRecord::class.java
        )
        val response = call.blockingFirst()
        expectThat(response.status)
            .isEqualTo(HttpStatus.CREATED)

        verify(cacheProxy, times(1)).save(phoneNumber, 1)
        verify(exactly = 1) { Parser.clean("(Home) 6048357354") }


    }

    @Test
    fun `Return 404 empty string message for empty input`() {
        val call: HttpResponse<String> = makeErrorCallWithGivenBody("{}")
        every { Parser.clean("") } returns mapOf()
        expectThat(call.status).isEqualTo(HttpStatus.BAD_REQUEST)
        expectThat(call.body()).isEqualTo(
            "Empty json provided! " +
                    "Please provided a json string of the following format"
        )
        verifyZeroInteractions(cacheProxy)
        verify(exactly = 0) { Parser.clean(any()) }
    }

    @Test
    fun `Return 404 invalid json message for invalid json`() {
        val call: HttpResponse<String> =
            makeErrorCallWithGivenBody(
                "Provided input was not of expected input"
            )
        expectThat(call.status).isEqualTo(HttpStatus.BAD_REQUEST)
        expectThat(call.body()).isEqualTo(
            "Provided input was not of expected input"
        )

        verify(exactly = 0) { Parser.clean(any()) }
        verifyZeroInteractions(cacheProxy)

    }

    @Test
    fun `Return 404 invalid raw_phone_numbers for bad input `() {
        val call: HttpResponse<String> =
            makeErrorCallWithGivenBody(
                "{\"raw_phone_numbers\":\"\"}"
            )
        expectThat(call.status).isEqualTo(HttpStatus.BAD_REQUEST)
        expectThat(call.body()).isEqualTo(
            "raw_phone_numbers contained zero valid inputs"
        )

        verify(exactly = 1) { Parser.clean("") }
        verifyZeroInteractions(cacheProxy)

    }

    @Test
    fun `return server error when storage service fails`() {
        `when`(cacheProxy.save(phoneNumber,1))
            .thenThrow(StoragePersistException::class.java)

        val call: HttpResponse<String> =
            makeErrorCallWithGivenBody(
                jsonString
            )

        expectThat(call.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        expectThat(call.body()).isEqualTo(
            "Database down, please try again later"
        )

        verify(exactly = 1) { Parser.clean("(Home) 6048357354") }
        verify(cacheProxy, times(1)).save(phoneNumber, 1)

    }


    private fun makeErrorCallWithGivenBody(body: String): HttpResponse<String> {
        return client.exchange(
            HttpRequest.POST(
                "/phone-numbers",
                body,
            ),
            String::class.java
        ).onErrorReturn { t -> (t as HttpClientResponseException).response as HttpResponse<String> }
            .blockingFirst()
    }

    @MockBean(StorageService::class)
    fun cacheProxy(): StorageService {
        return mock(StorageService::class.java)
    }
}

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
