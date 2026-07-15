package io.github.vooft.spektor.merger

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Paths
import kotlin.io.path.readText

class OpenApiValidatingParserTest : ShouldSpec({

    should("fail when a path item references a missing file") {
        val location = Paths.get(
            requireNotNull(OpenApiValidatingParserTest::class.java.getResource("/unresolvable-ref-specs/unified.yaml")).toURI()
        )

        val exception = shouldThrow<InvalidOpenApiSpecException> {
            OpenApiValidatingParser.parseAndValidate(location.readText(), location)
        }

        exception.file shouldBe location
        exception.messages shouldHaveSize 1
        exception.messages.single() shouldContain "does-not-exist.yaml"
    }
})
