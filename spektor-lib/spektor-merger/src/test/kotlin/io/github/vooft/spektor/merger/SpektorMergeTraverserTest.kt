package io.github.vooft.spektor.merger

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class SpektorMergeTraverserTest : ShouldSpec({

    should("fail when spec is invalid") {
        val specRoot = Paths.get(
            requireNotNull(SpektorMergeTraverserTest::class.java.getResource("/invalid-specs")).toURI()
        )
        val traverser = SpektorMergeTraverser(specRoot)

        val exception = shouldThrow<InvalidOpenApiSpecException> {
            traverser.resolveAllSpecFiles("unified")
        }

        exception.file shouldBe specRoot.resolve("invalid-spec.yaml")
        exception.messages shouldBe listOf("attribute paths.'/book'(get).responses is not of type `object`")
    }
})
