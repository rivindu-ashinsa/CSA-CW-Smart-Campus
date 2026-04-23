# Project title
CSAwebApp - Smart Building Sensor Management REST API

## Overview of API design
CSAwebApp is a REST API for managing smart-building data across Rooms, Sensors, and Sensor Readings. The design uses resource-oriented endpoints, JSON request/response payloads, and clear HTTP status handling for success and error conditions. Data is stored in an in-memory `MockDatabase` implemented with `HashMap`, which makes the project lightweight and suitable for coursework demonstrations.

## Packages used
### Internal application packages
- `model`: Java POJOs (`Room`, `Sensor`, `SensorReading`)
- `dao`: in-memory data layer (`MockDatabase`)
- `resource`: REST endpoint classes
- `exception`: custom domain exceptions
- `mapper`: exception mappers that return JSON error responses
- `filter`: request/response logging filter

### Maven dependencies
- `org.glassfish.jersey.inject:jersey-hk2:2.32`
- `org.glassfish.jersey.containers:jersey-container-servlet:2.32`
- `org.glassfish.jersey.media:jersey-media-json-jackson:2.32`

## How to build
1. Clone the repository:
   ```bash
   git clone https://github.com/rivindu-ashinsa/CSAwebApp.git
   cd CSAwebApp
   ```
2. Open the project in NetBeans or IntelliJ IDEA.
3. Ensure Java 8+ is installed.
4. Ensure Maven is installed:
   ```bash
   mvn -v
   ```
5. Build the WAR artifact:
   ```bash
   mvn clean package
   ```

## How to run Tomcat
1. Locate the generated file: `target/CSAwebApp.war`.
2. Copy `CSAwebApp.war` into Apache Tomcat's `webapps` directory.
3. Start Tomcat.
4. Confirm deployment is complete, then call the API using the base URL below.

## Base URL
```text
http://localhost:8080/CSAwebApp/api/v1
```

## Endpoints summary
- `GET /api/v1/` - API info
- `GET /api/v1/rooms` - list rooms
- `GET /api/v1/rooms/{id}` - get one room
- `POST /api/v1/rooms` - create room
- `DELETE /api/v1/rooms/{id}` - delete room
- `GET /api/v1/sensors` - list sensors (supports `?type=` filter)
- `GET /api/v1/sensors/{id}` - get one sensor
- `POST /api/v1/sensors` - create sensor
- `GET /api/v1/sensors/{sensorId}/readings` - list sensor readings
- `POST /api/v1/sensors/{sensorId}/readings` - add sensor reading

Common status codes used by this API: `200`, `201`, `403`, `404`, `409`, `422`.

## At least 5 curl commands

### Create Room
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"R1\",\"name\":\"Library\",\"capacity\":100}"
```

### Get Rooms
```bash
curl http://localhost:8080/CSAwebApp/api/v1/rooms
```

### Create Sensor
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"S1\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"R1\"}"
```

### Filter Sensors
```bash
curl http://localhost:8080/CSAwebApp/api/v1/sensors?type=Temperature
```

### Add Reading
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/sensors/S1/readings -H "Content-Type: application/json" -d "{\"value\":24.8}"
```

### Get Readings
```bash
curl http://localhost:8080/CSAwebApp/api/v1/sensors/S1/readings
```

### Delete Room
```bash
curl -X DELETE http://localhost:8080/CSAwebApp/api/v1/rooms/R1
```

### Get Single Sensor
```bash
curl http://localhost:8080/CSAwebApp/api/v1/sensors/S1
```

## Git history present
Recent commits exist in this repository and demonstrate active version history.

Example check:
```bash
git log --oneline -5
```

Current data note: since `MockDatabase` is in-memory, all data resets when the server restarts.
