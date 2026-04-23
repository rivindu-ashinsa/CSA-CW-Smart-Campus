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
In JAX-RS, resource classes are request-scoped by default, so the runtime generally creates a new instance of the resource class for each incoming HTTP request. In this project, that model fits well because the resource classes such as `RoomResource` and `SensorResource` are written as stateless handlers, meaning they do not keep mutable per-client state inside class fields.

The important architectural detail is that shared application data is not stored in resource instances; it is centralized in `MockDatabase` as static collections. Specifically, the application uses static `HashMap` objects for rooms and sensors, and a `HashMap` of `List<SensorReading>` for readings. This design makes data accessible across requests, but it also means concurrency control is not guaranteed by default because `HashMap` and `ArrayList` are not thread-safe for concurrent modifications. In a high-concurrency production setup, this could lead to race conditions or inconsistent writes. For coursework, the approach is acceptable because it keeps the design simple and transparent, but the production-safe progression would be to use thread-safe structures or a proper transactional persistence layer.

---

## 1.2 Question
Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design?

## Answer
HATEOAS is considered an advanced REST principle because it shifts API navigation responsibility from static client assumptions to server-provided links. Instead of hardcoding endpoint paths for every interaction, a client can discover what actions are possible by following links returned in responses.

This project already demonstrates a foundational version of that idea through `RootResource`, where `GET /api/v1/` returns metadata and resource links for `rooms` and `sensors`. That gives clients a discoverable entry point to the API. A more complete HATEOAS implementation would extend this pattern to entity-level responses, for example by including links to related resources such as a room's sensors or a sensor's readings. Even in its current form, the project reflects the core rationale: the API itself communicates navigation options instead of relying entirely on external documentation.

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
Yes, the DELETE behavior implemented for rooms is idempotent from a state perspective. When `DELETE /rooms/{id}` is called for an existing room that has no linked sensors, the room is removed and the response is successful. If the same request is sent again, the room no longer exists, so the endpoint returns `404 Not Found`. Although the response status differs between the first and subsequent calls, the server state does not continue changing after the first successful deletion, which is the key requirement for idempotency.

The implementation also enforces a domain rule before deletion: if the room still has associated sensors, deletion is blocked and `RoomNotEmptyException` is mapped to `409 Conflict`. This maintains referential integrity in the in-memory model.

---

# Part 3: Sensor Operations & Filtering

## 3.1 Question
What happens if wrong content type is sent?

## Answer
When a request body is sent with an unsupported media type, JAX-RS/Jersey typically rejects it before the resource method executes. In this project, resource classes that process request bodies are annotated with `@Consumes(MediaType.APPLICATION_JSON)`, so JSON is the expected input format for POST operations.

If a client submits a non-JSON payload, the framework response is generally `415 Unsupported Media Type`. This behavior is valuable because it protects business logic from receiving unexpected formats and enforces a consistent contract at the framework boundary. GET requests are less affected because they usually do not rely on request bodies.

---

## 3.2 Question
Why use @QueryParam instead of path variables for filtering?

## Answer
`@QueryParam` is semantically appropriate for optional filtering of collection resources. In `SensorResource#getAllSensors`, the `type` filter is optional: when omitted, the endpoint returns all sensors, and when provided (for example, `?type=Temperature`), it narrows results to matching sensor types using case-insensitive comparison.

This preserves the stable identity of the collection endpoint (`/sensors`) while allowing flexible filtering criteria to be layered on top. Path parameters are better suited for required identity segments such as `/sensors/{id}`, where the request targets one specific resource instance. Using query parameters for optional filters therefore keeps the API cleaner and more extensible.

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

The GET operation returns the existing list for that sensor, and when no readings are present it returns an empty list instead of an error, which gives predictable client behavior. The POST operation performs several domain checks and normalization steps: it first verifies the sensor exists (otherwise `404`), then blocks updates if the sensor is in `MAINTENANCE` state (mapped to `403`), auto-generates a reading ID when missing, auto-sets the timestamp when omitted, stores the reading in the correct sensor list, and finally updates the sensor's `currentValue` to reflect the latest measurement. This design supports both historical tracking and quick access to current sensor state.

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
Filters are ideal for logging because logging is a cross-cutting concern that applies to every endpoint, not just one resource method. In this project, `LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter`, so it logs request metadata (HTTP method and path) as well as response metadata (status code).

By registering the filter with `@Provider`, logging behavior is applied centrally and consistently without duplicating code across resource classes. This keeps controllers focused on business logic, reduces repetition, and provides a unified trace of request-response flow that is useful during debugging, testing, and operational analysis.

---
