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
In JAX-RS, resource classes are request-scoped by default, so the runtime typically creates a new resource object for each incoming request. In this project, `RoomResource` and `SensorResource` are stateless classes (they do not keep mutable instance fields between requests), which fits that lifecycle model.

The shared state is instead centralized in `MockDatabase` as static collections:
- `Map<String, Room> rooms`
- `Map<String, Sensor> sensors`
- `Map<String, List<SensorReading>> readings`

Because these are plain `HashMap` and `ArrayList`, they are not thread-safe under concurrent writes. This means simultaneous requests could cause race conditions (for example, two writes to the same map key, or concurrent list updates). For coursework/demo scope this is acceptable, but in production you would use thread-safe structures (`ConcurrentHashMap`, synchronized lists), or move to a persistent database with transaction control.

---

## 1.2 Question
Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design?

## Answer
HATEOAS is considered advanced REST design because the server guides the client through available next actions using links in responses, rather than forcing the client to hardcode endpoint knowledge.

In this codebase, there is an initial HATEOAS-style entry point in `RootResource`, where `GET /api/v1/` returns links for `rooms` and `sensors`. That already improves discoverability. A more complete HATEOAS design would also return contextual links inside domain responses, such as:
- room response contains `self`, `sensors`
- sensor response contains `self`, `readings`, `room`
- reading response contains `self`, `sensor`

So the project demonstrates the concept at the API root, and can be extended to full hypermedia navigation across all entities.

---

# Part 2: Room Management

## 2.1 Question
Implications of returning only IDs versus full objects.

## Answer
The trade-off is between payload size and number of round trips:
- IDs-only responses are smaller and cheaper per response, but require extra follow-up requests to resolve details.
- Full-object responses are larger, but reduce client-side orchestration and extra HTTP calls.

In this implementation:
- `GET /rooms` returns full `Room` objects, including `id`, `name`, `capacity`, and `sensorIds`.
- `GET /sensors` returns full `Sensor` objects.
- Rooms store only sensor IDs (`sensorIds`) instead of embedding full sensor objects, which is a balanced design: room payload stays compact while sensor details can still be fetched from `/sensors`.

This design is practical for coursework because it keeps the API simple while still modeling relationships explicitly.

---

## 2.2 Question
Is DELETE idempotent?

## Answer
Yes, DELETE is idempotent in this API.

`DELETE /rooms/{id}` behavior:
- If room exists and has no linked sensors: room is removed and API returns `200` with `"Room Deleted"`.
- If the same DELETE is sent again: room is already absent, API returns `404`.

The observable response code changes (200 then 404), but the server state after the first successful deletion remains unchanged on subsequent requests, which matches idempotency semantics.

One business-rule exception exists before deletion: if room still has sensor links, `RoomNotEmptyException` is thrown and mapped to `409 Conflict`.

---

# Part 3: Sensor Operations & Filtering

## 3.1 Question
What happens if wrong content type is sent?

## Answer
For endpoints that consume request bodies, wrong `Content-Type` causes JAX-RS/Jersey to reject the request before method execution.

In this project, resource classes are annotated with `@Consumes(MediaType.APPLICATION_JSON)`, so POST operations expect JSON. If a client sends XML or plain text to these endpoints, the typical response is:

`415 Unsupported Media Type`

GET endpoints are not body-driven, so they are generally unaffected by request `Content-Type` unless a body is incorrectly enforced by a client/proxy.

---

## 3.2 Question
Why use @QueryParam instead of path variables for filtering?

## Answer
`@QueryParam` is the correct choice for optional filtering of a collection resource.

In `SensorResource#getAllSensors`, `type` is optional:
- no query param -> returns all sensors
- `?type=Temperature` -> returns only matching sensor types (case-insensitive)

This keeps the base resource identity stable (`/sensors` still means "sensor collection") while expressing filter criteria cleanly. Path params are better for identifying a specific resource instance (for example, `/sensors/{id}`), not for optional search constraints.

---

# Part 4: Sub-Resource Design

## 4.1 Question
Benefits of Sub-Resource Locator pattern.

## Answer
The sub-resource locator pattern helps model nested resources without overcrowding parent resource classes.

In this project, `SensorResource` delegates reading operations through:
- `@Path("/{sensorId}/readings")`
- returns `new SensorReadingResource(sensorId)`

Benefits in this codebase:
- clear ownership: sensor endpoints remain in `SensorResource`, reading endpoints live in `SensorReadingResource`
- cleaner code separation and easier testing
- straightforward extension path for future reading-specific operations (pagination, time-range filters, delete single reading)

---

## 4.2 Question
Sensor readings design explanation.

## Answer
Readings are designed as a child resource of sensors: `/sensors/{sensorId}/readings`.

Implementation details:
- Storage: `MockDatabase.readings` is a map keyed by `sensorId`, each value is a `List<SensorReading>`.
- `GET /sensors/{sensorId}/readings` returns the list for that sensor; if none exists yet, it returns an empty list (not an error).
- `POST /sensors/{sensorId}/readings`:
	- returns `404` if sensor does not exist
	- returns `403` via mapper if sensor status is `MAINTENANCE`
	- auto-generates `id` if missing
	- auto-generates `timestamp` if `0`
	- appends the reading to the sensor-specific list
	- updates the parent sensor's `currentValue` to the new reading value

This gives both historical data retention and fast access to latest sensor state.

---

# Part 5: Error Handling & Logging

## 5.1 Question
Why use HTTP 422?

## Answer
`422 Unprocessable Entity` is used when the request syntax is valid JSON but violates domain semantics.

In this API, creating a sensor with a non-existent `roomId` triggers `LinkedResourceNotFoundException`, which is mapped by `LinkedResourceNotFoundMapper` to:

- status: `422`
- body: `{"error":"Linked room not found"}`

This is more precise than `400` because the payload structure itself is valid; only the relationship is invalid.

---

## 5.2 Question
Security risk of stack traces.

## Answer
Returning stack traces in API responses is a security risk because it can expose:
- internal package/class names
- framework and dependency details
- implementation flow and failure points

That information helps attackers fingerprint the system and plan targeted exploits. In this project, exception mappers return controlled JSON error messages (for example, `"Sensor unavailable"`) instead of stack traces, which is a safer pattern for public-facing APIs.

---

## 5.3 Question
Why use filters for logging?

## Answer
Filters provide centralized cross-cutting behavior, which keeps resource methods focused on business logic.

Here, `LoggingFilter` implements both:
- `ContainerRequestFilter` (logs incoming method + path)
- `ContainerResponseFilter` (logs outgoing HTTP status)

Because it is annotated with `@Provider`, logging applies across endpoints without duplicating print/log statements in each resource method. This improves maintainability and gives consistent request/response traceability for debugging.

---