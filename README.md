![Build and test](https://github.com/vooft/spektor/actions/workflows/build.yml/badge.svg?branch=main)
![Releases](https://img.shields.io/github/v/release/vooft/spektor)
![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/io.github.vooft.spektor)
![License](https://img.shields.io/github/license/vooft/spektor)

## Spektor - Ktor server code generation from OpenAPI specification

Spector is a Gradle plugin that generates Ktor server code from OpenAPI specification.
It generates a strongly typed API interface and routing code that can be used to implement the server logic.
Also, unified OpenAPI spec YAML is generated from all given specifications. It's placed into the `resources` of the
generated source with the same dir structure as `specRoot` is relative to your sources set `resources`.

Both Ktor 2 and Ktor 3 are supported.

## Usage

### Gradle plugin

Add the spektor plugin to your build file:

```kotlin
plugins {
    id("io.github.vooft.spektor") version "<version>"
}
```

Latest version could be found on [Gradle plugins portal](https://plugins.gradle.org/plugin/io.github.vooft.spektor).

### Generator configuration

```kotlin
spektor {
    // this is the only required parameter, all *.yaml files with paths will be processed
    specRoot = file("src/main/resources/openapi")

    // base api for all generated classes, rest of the package will be generated from the spec file path
    // default is "spektor.example"
    basePackage = "com.example.api"

    // suffix for generated DTO classes
    // optional default is "Dto"
    dtoSuffix = "Dto"

    // suffix for generated API interface and Routes class
    // optional default is "ServerApi"
    serverApiSuffix = "ServerApi"

    // suffix for generated Routes class
    // optional default is "Routes"
    routesSuffix = "Routes"

    // name for unified .yaml spec file
    // optional default is "openapi"
    unifiedSpecName = "openapi"
    
    // title used for unified spec
    // optional default is "Unified API"
    unifiedSpecTitle = "Unified API"
    
    // description used for unified spec
    // optional default is "Unified API"
    unifiedSpecDescription = "Unified API"
    
    // server urls used for unified spec
    // optional default is empty list
    unifiedSpecServers = listOf("https://example.com")
    
    // fail gradle :spektorMerge task if there was an error during spec unification
    // if disabled, error will still be logged
    // optional default is "true"
    failOnMergeError = true

    // optional class names substitutions
    substitutions {
        // replace a class for a whole DTO, provided class will be used everywhere the model is referenced
        ref {
            file = file("src/main/resources/openapi/models/money.yaml")
            model = "Money"
        } with "io.github.vooft.spektor.sample.models.Money"

        // replace a class for a specific property of a model, provided class will be used only for that property
        microtype {
            file = file("src/main/resources/openapi/models/book.yaml")
            model = "Book"
            property = "id"
        } with "io.github.vooft.spektor.sample.models.BookId"
    }
}
```

### Generated code structure

Since Ktor is not a declarative framework, `spektor` adds a layer of abstraction to make it easier to implement the
server logic.

For every OpenAPI tag, it will generate at least 2 classes (using Author example):

* `AuthorServerApi` - an interface with methods for every operation in the tag.
  The method parameters and return types are strongly typed using generated DTO classes.
  This interface should be implemented to provide the server logic.
* `AuthorRoutes` - a class with methods to create Ktor routes for every operation in the tag.
  This class takes an instance of `AuthorServerApi` in the constructor and uses it to handle the requests.
  The methods in this class should be used in the Ktor routing DSL.

Routes class is generated to be able to configure custom interceptors, authentication, etc when configuring the Ktor
routes.

Please see full example in [spektor-sample](./spektor-sample):

* Implementation of the ServerApi
  interface: [AuthorRestService.kt](./spektor-sample/src/main/kotlin/io/github/vooft/spektor/sample/apis/AuthorRestService.kt) --
  implements a generated class for the Author rest API, generated
  from [author.yaml](./spektor-sample/src/main/resources/openapi/api/author.yaml)
* Using the generated Routes class in Ktor
  routing: [Routing.kt](./spektor-sample/src/main/kotlin/io/github/vooft/spektor/sample/ktor/Routing.kt) -- uses the
  generated `AuthorRoutes` class to create Ktor routes.
