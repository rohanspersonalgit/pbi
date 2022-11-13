package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.doordash.interview.phone_number_parser.proxy.CacheProxy
import com.doordash.interview.phone_number_parser.proxy.CacheProxyImpl
import com.doordash.interview.phone_number_parser.proxy.PhoneNumberRecord
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import io.micronaut.test.annotation.MockBean
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.lang.RuntimeException
import javax.inject.Inject

@MicronautTest
class PhoneNumberControllerTest
 {
     @Inject
     lateinit var cacheProxy: CacheProxy

    @field:Client("/") @Inject lateinit var client : RxHttpClient

    val phoneNumber = PhoneNumber(
        "6048357354",
        PhoneType.HOME
    )

     val phoneNumberRecord = PhoneNumberRecord(
          phoneNumber.phoneType.value + phoneNumber.number,
         phoneNumber.number,
         phoneNumber.phoneType,
         1
     )
    @Test
    fun `something test`(){
        `when`(cacheProxy.save(phoneNumber, 1))
            .thenReturn(phoneNumberRecord)
        println(phoneNumber)
        println(phoneNumberRecord)
//        val resp = client.toBlocking().retrieve(HttpRequest.POST("/phone-numbers", "{\"rawInputJson\": \"(Home) 6048357354\"}"),PhoneNumberRecord::class.java)
        val call = client.exchange(
            POST("/phone-numbers",PhoneNumberRecord::class.java)
        )

        val response = call.blockingFirst()
        // use this to check status
//        response.status

//        expectThat(resp).isEqualTo(phoneNumberRecord)
    }

     @MockBean(CacheProxyImpl::class)
     fun cacheProxy(): CacheProxy {
         return mock(CacheProxy::class.java)
     }
}

