package io.github.vooft.spektor.codegen.codegen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ResponseNameGeneratorTest {
    @Test
    fun `should generate when operationId is camelCase`() {
        ResponseClassNameGenerator.generate("searchBooks") shouldBe "SearchBooksResponse"
    }

    @Test
    fun `should generate when operationId is single lowercase word`() {
        ResponseClassNameGenerator.generate("create") shouldBe "CreateResponse"
    }

    @Test
    fun `should generate when operationId is PascalCase`() {
        ResponseClassNameGenerator.generate("GetAuthor") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is hyphen separated`() {
        ResponseClassNameGenerator.generate("get-author") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is underscore separated`() {
        ResponseClassNameGenerator.generate("list_all_books") shouldBe "ListAllBooksResponse"
    }

    @Test
    fun `should generate when operationId has mixed separators`() {
        ResponseClassNameGenerator.generate("get_author-books") shouldBe "GetAuthorBooksResponse"
    }

    @Test
    fun `should generate when operationId has multiple consecutive separators`() {
        ResponseClassNameGenerator.generate("get--author") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is space separated`() {
        ResponseClassNameGenerator.generate("get author books") shouldBe "GetAuthorBooksResponse"
    }

    @Test
    fun `should throw when operationId is empty`() {
        shouldThrow<IllegalArgumentException> { ResponseClassNameGenerator.generate("") }
    }

    @Test
    fun `should throw when operationId is blank`() {
        shouldThrow<IllegalArgumentException> { ResponseClassNameGenerator.generate("   ") }
    }
}
