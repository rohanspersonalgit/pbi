package com.doordash.interview.phone_number_parser.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer

@MicronautTest
class PhoneNumberControllerTest(private val client: RxHttpClient, private val server: EmbeddedServer) {
    // TODO: Add your tests here.

    @Test
    fun `POST should return X when Y`() {
        val resp = client.toBlocking().retrieve(HttpRequest.POST("${server.url}/phone-numbers", ""))
        expectThat(resp).isEqualTo("")
    }
}
