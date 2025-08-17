### Usage

#### Implement the ServerApi interface
```kotlin
class EntityRestService(
    private val entityRepo: EntityRepository,
) : EntityServerApi {

    override suspend fun createEntity(request: Requests.CreateEntityRequest): ResponseEntity<Responses.EntityResponse> {
        val entity = entityRepo.create(request.name)
        return ResponseEntity.ok(entity.toApi())
    }

    override suspend fun updateEntity(id: UUID, request: Requests.UpdateEntityRequest): ResponseEntity<Responses.EntityResponse> {
        val entity = entityRepo.update(id, request.name)
        return ResponseEntity.ok(entity.toApi())
    }

    override suspend fun listEntity(): ResponseEntity<Responses.ListEntityResponse> {
        val entities = entityRepo.findAll()
        return ok(Responses.ListEntityResponse(entities.toApi()))
    }
}
```

#### Create routing and use the generated Routes class
```kotlin
// configure Routes instance
val entityRepo = EntityRepository(...)
val entityRestService = EntityRestService(entityRepo)
val entityRoutes = EntityRoutes(entityRestService)

// create a resource and use it in routing
class EntityResource(private val routes: EntityRoutes) {
    fun Route.routes() {
        routing {
            authentication("admin") {
                routes.createEntity()
                routes.updateEntity()
            }
            
            authentication("user") {
                routes.listEntities()
            }
        }
    }
}
```

### Static code

#### Base interface for the generated API
```kotlin
// interface marker
interface OpenApiServerApi

// accessor for the ktor principal
suspend fun <P: Principal> OpenApiServerApi.principal(klass: KClass<P>): P? {
    val value = requireNotNull(currentCoroutineContext()[ApplicationCallElement]) {
        "${ApplicationCallElement::class} element was not found in the context element, is the service called from the correct place?"
    }

    return value.applicationCall.authentication.principal(null, klass)
}

suspend inline fun <reified P: Principal> OpenApiServerApi.principal(): P? = principal(P::class)

// coroutine context element for storing the application call
class ApplicationCallElement(
    val applicationCall: ApplicationCall
) : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<ApplicationCallElement>
    override val key: CoroutineContext.Key<ApplicationCallElement> = ApplicationCallElement
}

data class ResponseEntity<T>(val status: HttpStatusCode, val body: T, val typeInfo: TypeInfo) {
    companion object {
        inline fun <reified T> ok(body: T) = ResponseEntity(HttpStatusCode.OK, body, typeInfo<T>())
    }
}
```


### Generated code

#### Generated API interface
```kotlin
interface EntityServerApi : OpenApiServerApi {
    suspend fun createEntity(request: Requests.CreateEntityRequest): ResponseEntity<Responses.EntityResponse>
    suspend fun updateEntity(id: UUID, request: Requests.UpdateEntityRequest): ResponseEntity<Responses.EntityResponse>
    suspend fun listEntities(): ResponseEntity<Responses.ListEntityResponse>
    
    object Requests {
        @Serializable
        data class CreateEntityRequest(val name: String)
        @Serializable
        data class UpdateEntityRequest(val name: String)
    }
    
    object Responses {
        @Serializable
        data class EntityResponse(val name: String, val createdAt: @Contextual Instant)
        @Serializable
        data class ListEntityResponse(val entities: List<EntityResponse>)
    }
}
```

#### Generated routes
```kotlin
class EntityRoutes(
    private val apiService: EntityServerApi
) {

    context(route: Route)
    fun createEntity() {
        route.post("/entities") {
            val request = call.receive<Requests.CreateEntityRequest>()
            val response = withApplicationCall { apiService.createEntity(request) }
            call.respond(response.status, response.body, response.typeInfo)
        }
    }

    context(route: Route)
    fun updateEntity() {
        route.put("/entities/{id}") {
            val request = call.receive<Requests.UpdateEntityRequest>()
            val id = UUID.fromString(call.param<String>(paramName))
            val response = withApplicationCall { apiService.updateEntity(id, request) }
            call.respond(response.status, response.body, response.typeInfo)
        }
    }

    context(route: Route)
    fun listEntities() {
        route.get("/entities") {
            val response = withApplicationCall { apiService.listEntities() }
            call.respond(response.status, response.body, response.typeInfo)
        }
    }

    private suspend fun <T> PipelineContext<Unit, ApplicationCall>.withApplicationCall(block: suspend () -> T): T {
        return withContext(ApplicationCallElement(call)) {
            block()
        }
    }
}
```
