package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.proxy.CacheProxy
import com.doordash.interview.phone_number_parser.proxy.CacheProxyImpl
import io.kotest.core.spec.style.StringSpec
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk
import javax.inject.Inject

//@MicronautTest
class PhoneNumberControllerTest(
    private val cacheProxy: CacheProxy,
    @Client("/") private val client: RxHttpClient,
//    private val server: EmbeddedServer
    ) : StringSpec( {

    @Inject
    @field:Client("/")
    lateinit var client : RxHttpClient

    "POST should return X when Y" {

        val resp = client.toBlocking().retrieve(HttpRequest.POST("/phone-numbers", "{\"rawInputJson\": \"(Home) 60483573254\"}") )
        expectThat(resp).isEqualTo("[]")
    }
})
 {
       @MockBean(CacheProxyImpl::class)
        fun cacheProxy(): CacheProxy {
            return mockk()
        }
    }

//object ProjectConfig : AbstractProjectConfig() {
//    override fun listeners() = listOf(MicronautKotestExtension)
//    override fun extensions() = listOf(MicronautKotestExtension)
//}