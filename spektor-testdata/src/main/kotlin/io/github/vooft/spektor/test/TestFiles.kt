package io.github.vooft.spektor.test

import java.nio.file.Path
import java.nio.file.Paths

object TestFiles {
    val listFile: Path = Paths.get("../spektor-testdata/src/test/resources/api/list-book.yaml")
    val pathVarFile: Path = Paths.get("../spektor-testdata/src/test/resources/api/path-var-author.yaml")
    val requestBodyFile: Path = Paths.get("../spektor-testdata/src/test/resources/api/request-body-book.yaml")
    val bookModelFile: Path = Paths.get("../spektor-testdata/src/test/resources/models/book.yaml")
    val authorModelFile: Path = Paths.get("../spektor-testdata/src/test/resources/models/author.yaml")
}
