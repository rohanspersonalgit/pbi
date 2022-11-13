package com.doordash.interview.phone_number_parser.parser

import org.junit.jupiter.api.Test
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions

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
    fun`handle a perfectly formatted input string with a single output`(){
        val parser = Parser(perfectCase1.input)
        val res = parser.clean()


        checkResults(res,
        mapOf(
            perfectCase1.expectedOutput to 1
        ))
    }

    @Test
    fun`handle a perfectly formatted input string with multiple outputs`(){
        val testCases = listOf(perfectCase1, perfectCase2, perfectCase3)
        val parser = Parser(testCases.map { it.input }.toString())
        val res = parser.clean()
        checkResults(res, mapOf(
            perfectCase1.expectedOutput to 1,
            perfectCase2.expectedOutput to 1,
            perfectCase3.expectedOutput to 1
        ))
    }

    @Test
    fun `handle a case where there are dashes in the number`(){
        val testCase = ParserInputToExpectedOutput(
            "(Cell) 604-805-7254",
            PhoneNumber(
                "6048057254",
                PhoneType.CELL
            )
        )

        val res = Parser(testCase.input).clean()

        checkResults(res, mapOf(testCase.expectedOutput to 1))
    }

    @Test
    fun `return none when there is a invalid phone type`(){
        val inputToExpectedOutput = "(Home6048057254"
        val res = Parser(inputToExpectedOutput).clean()
        Assertions.assertEquals(
            0,
            res.size
        )
    }

    @Test
    fun `return none when there is a invalid phone number`(){
        val res = Parser("(Home)604805625").clean()
        Assertions.assertEquals(
            0,
            res.size
        )
    }

    @Test
    fun `return 1 valid 1 invalid value`(){
        val res = Parser("(Home)604805762" + perfectCase1.input).clean()
        Assertions.assertEquals(
            1,
            res.size
        )
        checkResults(res, mapOf(perfectCase1.expectedOutput to 1))
    }
}

fun checkResults(result: Map<PhoneNumber, Int>, expectedOutput: Map<PhoneNumber, Int>){
    Assertions.assertEquals(
        result.size,
        expectedOutput.size
    )
    expectedOutput.entries.forEach{
        Assertions.assertTrue(
            result.containsKey(it.key)
        )
        Assertions.assertEquals(
            it.value,
            result[it.key]
        )
    }
}

internal data class ParserInputToExpectedOutput(
    val input: String,
    val expectedOutput: PhoneNumber
)
