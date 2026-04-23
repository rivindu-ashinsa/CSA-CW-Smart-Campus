# CSAwebApp - Smart Building Sensor Management REST API

## Overview
CSAwebApp is a REST API for smart-building sensor management. It supports creating and managing rooms, assigning sensors to rooms, and storing historical sensor readings. The API follows RESTful design principles, communicates using JSON, and uses an in-memory `MockDatabase` (`HashMap`-based) for storage.

## API Design Overview

### Technology and Architecture
- Java Maven WAR project
- JAX-RS (Jersey)
- Apache Tomcat
- REST API with JSON request/response payloads
- In-memory persistence using `MockDatabase` with `HashMap`

### Package Structure
- `model`: Java POJOs (`Room`, `Sensor`, `SensorReading`)
- `dao`: data access layer with `MockDatabase`
- `resource`: REST endpoint controllers
- `exception`: custom exception classes
- `mapper`: exception-to-HTTP-response mappers
- `filter`: request/response logging filter

### Resource Hierarchy
- `/api/v1/rooms`
- `/api/v1/sensors`
- `/api/v1/sensors/{sensorId}/readings`

### HTTP Methods Used
- `GET`
- `POST`
- `DELETE`

### Status Codes Used
- `200 OK`
- `201 Created`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `422 Unprocessable Entity`

## Build Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/rivindu-ashinsa/CSAwebApp.git
   cd CSAwebApp
   ```
2. Open the project in NetBeans or IntelliJ IDEA.
3. Ensure Java 8+ is installed and configured.
4. Ensure Maven is installed (`mvn -v`).
5. Build the project:
   ```bash
   mvn clean package
   ```

## Run Server Instructions
1. Locate the generated WAR file at `target/CSAwebApp.war`.
2. Deploy `CSAwebApp.war` to Apache Tomcat's `webapps` directory.
3. Start Tomcat.
4. Access the API at:

```text
http://localhost:8080/CSAwebApp/api/v1
```

## Sample curl Commands

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

## Notes
- Data is stored in-memory using `MockDatabase`, so all data resets when the server restarts.
- This project is designed for coursework demonstration and API design practice.
