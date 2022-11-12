package com.doordash.interview.phone_number_parser.parser

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

@MicronautTest
class ParserTest : ShouldSpec( {
    val perfectCase1 = ParserInputToExpectedOutput(
        "(Cell) 604-805-7254",
        PhoneNumber(
            "6048057254",
            PhoneType.CELL
        )
    )

     val perfectCase2 = ParserInputToExpectedOutput(
        "(Home) 415-415-4155",
        PhoneNumber(
            "4154154155",
            PhoneType.HOME
        )
    )

     val perfectCase3 = ParserInputToExpectedOutput(
        "(Cell) 1231231234",
        PhoneNumber(
            "1231231234",
            PhoneType.CELL
        )
    )

    should("handle a perfectly formatted input string with a single output") {
        val parser = Parser(perfectCase1.input)
        val res = parser.clean()

        res.size shouldBe 1
        checkResults(res.first(), perfectCase1.expectedOutput)
    }

    should(" handle a perfectly formatted input string with multiple outputs"){
        val testCases = listOf(perfectCase1, perfectCase2, perfectCase3)
        val parser = Parser(testCases.map { it.input }.toString())
        val res = parser.clean()
        res.size shouldBe 3

        for (i in 0..2){
            checkResults(res[i], testCases[i].expectedOutput)
        }
    }

    should(" handle a case where there are dashes in the number"){
        val testCase = ParserInputToExpectedOutput(
            "(Cell) 604-805-7254",
            PhoneNumber(
                "6048057254",
                PhoneType.CELL
            )
        )

        val res = Parser(testCase.input).clean()
        res.size shouldBe 1
        checkResults(res.first(), testCase.expectedOutput)
    }

    should(" return none when there is a invalid phone type"){
        val inputToExpectedOutput = "(Home6048057254"
        val res = Parser(inputToExpectedOutput).clean()
        res.size.shouldBeZero()
    }

    should(" return none when there is a invalid phone number"){
        val res = Parser("(Home)604805625").clean()
        res.size shouldBe 0
    }

    should(" return 1 valid 1 invalid value"){
        val res = Parser("(Home)604805762" + perfectCase1.input).clean()
        res.size shouldBe 1
        checkResults(res.first(), perfectCase1.expectedOutput)
    }
})

fun checkResults(result: PhoneNumber, expectedOutput: PhoneNumber){
    result.number shouldBe expectedOutput.number
    result.phoneType shouldBe expectedOutput.phoneType
}

internal data class ParserInputToExpectedOutput(
    val input: String,
    val expectedOutput: PhoneNumber
)
