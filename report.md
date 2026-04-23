# 5COSC022W Coursework Report

## Student Details
- **Name:** H D R A Handuwala
- **Student ID:** w2120188 / 20231411
- **University:** Informatics Institute of Technology (IIT) Affiliated with University of Westminster

---

# Part 1: Service Architecture & Setup

## 1.1 Question
Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.

## Answer
JAX-RS, to use the terminology of JAX-RS, resource classes are request-scoped by default which means that at runtime another resource class instance will be created for every incoming HTTP request. That works for this project because the resource classes like `RoomResource` and `SensorResource` are implemented as stateless handlers, which means they do not maintain any mutable per-client state in fields inside of the class.

The important architectural point is that shared app data resides in `MockDatabase` as static collections instead of stored into resource instances. Specifically, it uses a `HashMap` of `List` for readings and static `HashMap` objects to store rooms and sensors. This design allows the data to be accessed across requests, but since `HashMap` and `ArrayList` are not thread-safe when updated concurrently so concurrency control doesn't come by default. This would lead to inconsistent writes or race situations in a high-concurrency production system.The method is appropriate for coursework since it maintains a straightforward and transparent design, but using thread-safe structures or an appropriate transactional persistence layer would be the production-safe evolution.
---

## 1.2 Question
Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design?

## Answer
HATEOAS is considered an advanced REST principle because it shifts API navigation responsibility from static client assumptions to server-provided links. Instead of hardcoding endpoint paths for every interaction, a client can discover what actions are possible by following links returned in responses.

This project has a working implementation of that idea in the form of `RootResource`, where `GET /api/v1/` returns some metadata and links to resources for rooms and sensors. That provides clients an entry point to the API that they can discover. An even fuller HATEOAS implementation would apply this closer to the entity level, for instance link to a room's sensors and a sensor's readings. The project, even in its current state, represents the fundamental reason: The API itself does not rely solely on external documentation but rather tells how to navigate.

---

# Part 2: Room Management

## 2.1 Question
Implications of returning only IDs versus full objects.

## Answer
Returning only identifiers can reduce response size and bandwidth usage, but it pushes additional complexity onto the client because extra requests are needed to resolve details. Returning full objects increases payload size, yet it usually simplifies client implementation and reduces the number of round trips required to render meaningful data.

In this API, the design is intentionally balanced. Endpoints such as `GET /rooms` and `GET /sensors` return full entity objects so clients can work with useful data immediately. At the same time, room-to-sensor relationships are represented by `sensorIds` rather than embedding complete sensor objects inside each room. This avoids heavy nesting while still making relationships explicit. For coursework scope, this is a practical compromise between payload efficiency, readability, and implementation simplicity.

---

## 2.2 Question
Is DELETE idempotent?

## Answer
The DELETE behaviour that we implement for rooms is state idempotent, yes. Success on `DELETE /rooms/{id}` for an existing room without linked sensors and the room is removed. If we send the same request again, since that room does not exist anymore, the endpoint `404 Not Found`. The first invocation results in a different response status but after the resource is deleted, the server state cannot further change, so this upholds the key principle for idempotency.

The implementation also enforces a domain rule before deletion: if the room still has associated sensors, deletion is blocked and `RoomNotEmptyException` is mapped to `409 Conflict`. This maintains referential integrity in the in-memory model.

---

# Part 3: Sensor Operations & Filtering

## 3.1 Question
What happens if wrong content type is sent?

## Answer
Usually, if an unsupported media type is sent with a request body then the JAX-RS/Jersey rejects it before even executing thr resource method. Here, we have resource classes that needs to consume request body annotated with `@Consumes(MediaType. APPLICATION_JSON)` the expected input type for `POST`.

In the case of a non-JSON payload submitted by the client, for example, a `415 Unsupported Media Type` serves as the common framework response. This behavior is not useless because it shields business logic by ensuring that only expected formats are sent along and that a consistent contract exists at the framewerk boundary. This means that they are less impacted by things like `GET` requests, which do not need to rely on request bodies.

---

## 3.2 Question
Why use @QueryParam instead of path variables for filtering?

## Answer
`@QueryParam` is semantically appropriate for optional filtering of collection resources. In `SensorResource#getAllSensors`, the `type` filter is optional: when omitted, the endpoint returns all sensors, and when provided (for example, `?type=Temperature`), it narrows results to matching sensor types using case-insensitive comparison.

This preserves the stable identity of the collection endpoint (`/sensors`) while allowing flexible filtering criteria to be layered on top. Path parameters are better suited for required identity segments such as `/sensors/{id}`, where the request targets one specific resource instance. Using query parameters for optional filters, therefore keeps the API cleaner and more extensible.

---

# Part 4: Sub-Resource Design

## 4.1 Question
Benefits of Sub-Resource Locator pattern.

## Answer
The Sub-Resource Locator pattern is beneficial because it separates parent-resource responsibilities from nested-resource behavior. In this implementation, `SensorResource` exposes `@Path("/{sensorId}/readings")` and returns a dedicated `SensorReadingResource` instance. This means sensor-level operations remain in one class, while reading-specific operations are encapsulated in another.

That separation improves maintainability, readability, and future scalability. As the API evolves, reading-specific enhancements such as pagination, date-range filtering, or deletion policies can be implemented in `SensorReadingResource` without overcomplicating `SensorResource`. The pattern also mirrors the domain structure naturally, since readings are conceptually subordinate to sensors.

---

## 4.2 Question
Sensor readings design explanation.

## Answer
Sensor readings are modeled as a nested resource under each sensor through `/sensors/{sensorId}/readings`, which correctly reflects ownership in the domain. Internally, readings are stored in `MockDatabase.readings`, where each sensor ID maps to a list of `SensorReading` records.

The GET returns the (possibly empty) list for that sensor, and when there are no readings it returns an empty list instead of an error, leading to predictable behavior in clients. Several domain checks and normalization steps occur behind the scenes when executing the `POST`: it checks if the sensor exists (otherwise `404`), blocks update if sensor in `MAINTENANCE` state (mapped to `403`), auto-generates a reading id if missing, auto-sets timestamp if omitted, stores in respective sensor list, updates `currentValue` of sensor with latest measurement. This design supports both historical tracking and quick access to current sensor state.

---

# Part 5: Error Handling & Logging

## 5.1 Question
Why use HTTP 422?

## Answer
HTTP `422 Unprocessable Entity` is used when a request is syntactically valid but semantically invalid for business rules. In this project, creating a sensor with a `roomId` that does not exist triggers `LinkedResourceNotFoundException`, which is mapped to `422` with the JSON message `{"error":"Linked room not found"}`.

This is a stronger semantic signal than a generic `400 Bad Request` because the JSON format and field structure are correct; only the domain relationship is invalid. Using `422` therefore improves API clarity for client developers and makes error handling more precise.

---

## 5.2 Question
Security risk of stack traces.

## Answer
Returning raw stack traces in API responses is risky because it can expose internal class names, package structure, framework details, and execution paths. Attackers can use this information to fingerprint the application and design targeted exploit attempts against known libraries or architectural weaknesses.

A safer pattern is to keep detailed technical traces in server logs while returning controlled, minimal error messages to clients. This project follows that principle through custom exception mappers, which return concise JSON errors such as `{"error":"Sensor unavailable"}` and `{"error":"Linked room not found"}` instead of leaking internal exception internals.

---

## 5.3 Question
Why use filters for logging?

## Answer
For this reason that `Logging` is a cross-cutting concern that applies to every endpoint (and not just one resource method), hence `Filters` are well suited for logging behavior. In this project, we have `LoggingFilter` which implements both `ContainerRequestFilter` and `ContainerResponseFilter`, so it logs request metadata (method + path) as well as response metadata (status code).

Centralized logging behavior through registration of the filter with `@Provider` which prevents to write same code at resource classes. This helps focus the controllers on business logic, minimizes redundancy, and provides a single trace of the request-response flow that is helpful in debugging, testing, and operational analysis.

---

