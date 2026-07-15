package io.github.vooft.spektor.merger

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readText

class SpektorMergerTest : ShouldSpec({

    should("merge specs and produce a valid unified spec") {
        val specRoot = Paths.get(
            requireNotNull(SpektorMergerTest::class.java.getResource("/merge-specs")).toURI()
        )
        val outputPath = tempdir().toPath()

        val result = SpektorMerger(
            unifiedSpecName = "unified",
            unifiedSpecTitle = "Unified API",
            unifiedSpecDescription = null,
            servers = listOf("http://localhost:8080"),
            specRoot = specRoot,
            outputPath = outputPath,
        ).merge()

        result.getOrThrow()

        val yamlOut = outputPath.resolve("unified.yaml")
        yamlOut.exists() shouldBe true

        val unified = OpenApiValidatingParser.parseAndValidate(
            content = yamlOut.readText(),
            location = specRoot.resolve("unified.yaml"),
        )
        unified.paths.keys.sorted() shouldContainExactly listOf("/author/{id}", "/book")
    }
})
