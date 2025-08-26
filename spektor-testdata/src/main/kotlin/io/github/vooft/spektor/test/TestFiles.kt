package io.github.vooft.spektor.test

import java.nio.file.Path
import java.nio.file.Paths

object TestFiles {
    val rootPath: Path = Paths.get("../spektor-testdata/src/test/resources")
    val listFile: Path = rootPath.resolve("api/list-book.yaml")
    val pathVarFile: Path = rootPath.resolve("api/path-var-author.yaml")
    val requestBodyFile: Path = rootPath.resolve("api/request-body-book.yaml")
    val bookModelFile: Path = rootPath.resolve("models/book.yaml")
    val authorModelFile: Path = rootPath.resolve("models/author.yaml")
}
