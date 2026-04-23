# 5COSC022W Coursework Report

## Student Details
- **Name:** [ENTER NAME]
- **Student ID:** [ENTER ID]
- **University:** [ENTER UNIVERSITY]

---

# Part 1: Service Architecture & Setup

## 1.1 Question
Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.

## Answer
By default, JAX-RS resource classes are **request-scoped**, meaning a new instance is created for each HTTP request. This ensures thread safety at the resource level because no shared state exists inside the resource class itself.

However, in this project, shared data is stored in a static `MockDatabase` using `HashMap` and `ArrayList`. Since these are shared across requests, concurrency issues could occur in a real-world scenario. For this coursework, this approach is sufficient because the system is running in a controlled environment.

---

## 1.2 Question
Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design?

## Answer
HATEOAS allows APIs to provide navigational links inside responses, enabling clients to discover actions dynamically without hardcoding endpoints.

Benefits:
- reduces dependency on documentation
- improves API discoverability
- supports easier version evolution
- improves client flexibility

---

# Part 2: Room Management

## 2.1 Question
Implications of returning only IDs versus full objects.

## Answer
Returning full objects increases payload size but reduces additional API calls. Returning only IDs reduces bandwidth but requires extra requests for details.

This project returns full objects for simplicity and usability.

---

## 2.2 Question
Is DELETE idempotent?

## Answer
Yes. Repeated DELETE requests produce the same system state. After deletion, subsequent requests return `404 Not Found` without further state changes.

---

# Part 3: Sensor Operations & Filtering

## 3.1 Question
What happens if wrong content type is sent?

## Answer
If a request is not `application/json`, JAX-RS cannot process it and returns:

`415 Unsupported Media Type`

---

## 3.2 Question
Why use @QueryParam instead of path variables for filtering?

## Answer
Query parameters are better for filtering collections because they preserve resource identity and allow multiple filters.

Example:
`/sensors?type=CO2`

---

# Part 4: Sub-Resource Design

## 4.1 Question
Benefits of Sub-Resource Locator pattern.

## Answer
Sub-resource locators improve modularity by delegating nested resources to separate classes. This improves maintainability and scalability.

---

## 4.2 Question
Sensor readings design explanation.

## Answer
Readings are stored per sensor using:

`/sensors/{sensorId}/readings`

POST adds a reading and updates sensor current value. GET retrieves historical data.

---

# Part 5: Error Handling & Logging

## 5.1 Question
Why use HTTP 422?

## Answer
422 is used when request format is valid but semantic data is incorrect, such as referencing a non-existent room.

---

## 5.2 Question
Security risk of stack traces.

## Answer
Stack traces expose internal system details and should not be returned to clients. They can reveal framework and file structure vulnerabilities.

---

## 5.3 Question
Why use filters for logging?

## Answer
Filters centralize logging logic, avoiding repetition and keeping resource classes clean. They apply globally to all endpoints.

---