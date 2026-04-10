package io.github.vooft.spektor.test

import java.nio.file.Path
import java.nio.file.Paths

object TestFiles {
    val rootPath: Path = Paths.get("../spektor-testdata/src/test/resources")

    /**
     * API
     */
    val listFile: Path = rootPath.resolve("api/list-book.yaml")
    val listBookByFiltersFile: Path = rootPath.resolve("api/list-book-by-filters.yaml")
    val pathVarFile: Path = rootPath.resolve("api/path-var-author.yaml")
    val requestBodyFile: Path = rootPath.resolve("api/request-body-book.yaml")
    val optionalRequestBodyFile: Path = rootPath.resolve("api/optional-request-body-book.yaml")
    val deleteBookFile: Path = rootPath.resolve("api/delete-book.yaml")
    val pathParamRefFile: Path = rootPath.resolve("api/path-param-ref.yaml")
    val queryParamRefFile: Path = rootPath.resolve("api/query-param-ref.yaml")
    val listOwnerFile: Path = rootPath.resolve("api/list-owner.yaml")

    /**
     * Models
     */
    val bookModelFile: Path = rootPath.resolve("models/book.yaml")
    val authorModelFile: Path = rootPath.resolve("models/author.yaml")
    val moneyModelFile: Path = rootPath.resolve("models/money.yaml")
    val countryPricesModelFile: Path = rootPath.resolve("models/country-prices.yaml")
    val ownerModelFile: Path = rootPath.resolve("models/owner.yaml")
    val listEventFile: Path = rootPath.resolve("api/list-event.yaml")
    val eventModelFile: Path = rootPath.resolve("models/event.yaml")
}
