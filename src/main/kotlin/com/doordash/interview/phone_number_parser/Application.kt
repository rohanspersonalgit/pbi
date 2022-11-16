package com.doordash.interview.phone_number_parser

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    build()
        .args(*args)
        .packages("com.doordash.interview.phone_number_parser")
        .start()
}

