package com.doordash.interview.phone_number_parser.parser

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import strikt.api.expectThat

@MicronautTest
class ParserTest {
    private val perfectCase1 = ParserInputToExpectedOutput(
        "(Cell) 604-805-7254",
        PhoneNumber(
            "6048057254",
            PhoneType.CELL
        )
    )

    private val perfectCase2 = ParserInputToExpectedOutput(
        "(Home) 415-415-4155",
        PhoneNumber(
            "4154154155",
            PhoneType.HOME
        )
    )

    private val perfectCase3 = ParserInputToExpectedOutput(
        "(Cell) 1231231234",
        PhoneNumber(
            "1231231234",
            PhoneType.CELL
        )
    )


    @Test
    fun `Should handle a perfectly formatted input string with a single output`() {
        val parser = Parser(perfectCase1.input)
        val res = parser.clean()

        Assertions.assertEquals(
            1,
            res.size,
            "Input size of expected but equals ${res.size}"
        )
        checkResults(res.first(), perfectCase1.expectedOutput)
    }

    @Test
    fun `Should handle a perfectly formatted input string with multiple outputs`(){
        val testCases = listOf(perfectCase1, perfectCase2, perfectCase3)
        val parser = Parser(testCases.map { it.input }.toString())
        val res = parser.clean()
        Assertions.assertEquals(
            3,
            res.size
        )

        for (i in 0..2){
            checkResults(res[i], testCases[i].expectedOutput)
        }
    }

    @Test
    fun `Should handle a case where there are dashes in the number`(){
        val testCase = ParserInputToExpectedOutput(
            "(Cell) 604-805-7254",
            PhoneNumber(
                "6048057254",
                PhoneType.CELL
            )
        )

        val res = Parser(testCase.input).clean()
        Assertions.assertEquals(
            res.size,
            1
        )
        checkResults(res.first(), testCase.expectedOutput)
    }

    @Test
    fun `Should return none when there is a invalid phone type`(){
        val inputToExpectedOutput = "(Home6048057254"
        val res = Parser(inputToExpectedOutput).clean()
        Assertions.assertEquals(
            res.size,
        0
        )
    }

    @Test
    fun `Should return none when there is a invalid phone number`(){
        val res = Parser("(Home)604805625").clean()

        Assertions.assertEquals(
            res.size,
            0
        )
    }

    @Test
    fun `Should return 1 valid 1 invalid value`(){
        val res = Parser("(Home)604805762" + perfectCase1.input).clean()

        Assertions.assertEquals(
            res.size,
            1
        )
        checkResults(res.first(), perfectCase1.expectedOutput)
    }
}

private fun checkResults(result: PhoneNumber, expectedOutput: PhoneNumber){
    Assertions.assertEquals(
        expectedOutput.number,
        result.number,
        "Expected 6048057254 but got ${result.number}"
    )

    Assertions.assertEquals(
        expectedOutput.phoneType,
        result.phoneType,
        "Unexpected phone type"
    )
}

internal data class ParserInputToExpectedOutput(
    val input: String,
    val expectedOutput: PhoneNumber
)
