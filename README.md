# CSAwebApp REST API

Public REST API for managing campus rooms, sensors, and sensor readings.

## GitHub Hosting

This project is intended to be hosted in a **public GitHub repository**.

After committing your code, publish it to GitHub:

```bash
git remote add origin https://github.com/<your-username>/CSAwebApp.git
git branch -M main
git push -u origin main
```

Replace `<your-username>` with your GitHub username.

## API Design Overview

### Stack
- Java 8
- Maven (WAR packaging)
- Jersey (JAX-RS)
- JSON via Jackson
- In-memory storage (`MockDatabase`) for demo/testing

### Base URL
When deployed as a WAR named `CSAwebApp` on local Tomcat:

```text
http://localhost:8080/CSAwebApp/api/v1
```

### Resource Model
- `Room`
  - `id` (string)
  - `name` (string)
  - `capacity` (number)
  - `sensorIds` (string array)
- `Sensor`
  - `id` (string)
  - `type` (string)
  - `status` (string)
  - `currentValue` (number)
  - `roomId` (string)
- `SensorReading`
  - `id` (string, auto-generated if omitted)
  - `timestamp` (epoch millis, auto-generated if omitted)
  - `value` (number)

### Main Endpoints
- `GET /` - API info and top-level links
- `GET /rooms` - list rooms
- `GET /rooms/{id}` - get room by ID
- `POST /rooms` - create room
- `DELETE /rooms/{id}` - delete room (only if empty)
- `GET /sensors` - list sensors (`?type=<sensorType>` supported)
- `GET /sensors/{id}` - get sensor by ID
- `POST /sensors` - create sensor (linked room must exist)
- `GET /sensors/{sensorId}/readings` - list readings for a sensor
- `POST /sensors/{sensorId}/readings` - add reading

### Error Handling
Custom exception mappers return JSON errors for business-rule failures:
- `409 Conflict`: room contains sensors (`{"error":"Room contains sensors"}`)
- `422 Unprocessable Entity`: linked room not found for sensor (`{"error":"Linked room not found"}`)
- `403 Forbidden`: sensor unavailable (`{"error":"Sensor unavailable"}`)

## Build and Launch (Step-by-Step)

### Prerequisites
1. Install Java 8+ and set `JAVA_HOME`.
2. Install Maven 3.6+.
3. Install Apache Tomcat 9+ (local).

### 1. Build the project
From the project root:

```bash
mvn clean package
```

Expected artifact:
- `target/CSAwebApp.war`

### 2. Deploy WAR to Tomcat
Copy `target/CSAwebApp.war` into Tomcat's `webapps` directory.

Windows example:

```powershell
copy target\CSAwebApp.war C:\path\to\apache-tomcat\webapps\
```

### 3. Start Tomcat
From Tomcat `bin`:

Windows:

```powershell
.\startup.bat
```

Linux/macOS:

```bash
./startup.sh
```

### 4. Verify server is running
Open:

```text
http://localhost:8080/CSAwebApp/api/v1/
```

You should receive JSON with version/admin/resource links.

## Sample curl Commands (Successful Interactions)

Use these from a terminal after Tomcat is running.

### 1) Health/info endpoint
```bash
curl -X GET http://localhost:8080/CSAwebApp/api/v1/
```

### 2) Create a room
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"room-101","name":"Lab 101","capacity":30}'
```

### 3) List rooms
```bash
curl -X GET http://localhost:8080/CSAwebApp/api/v1/rooms
```

### 4) Create a sensor linked to that room
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"sensor-temp-1","type":"temperature","status":"ACTIVE","currentValue":0.0,"roomId":"room-101"}'
```

### 5) List sensors filtered by type
```bash
curl -X GET "http://localhost:8080/CSAwebApp/api/v1/sensors?type=temperature"
```

### 6) Add a reading to a sensor
```bash
curl -X POST http://localhost:8080/CSAwebApp/api/v1/sensors/sensor-temp-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.8}'
```

### 7) Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/CSAwebApp/api/v1/sensors/sensor-temp-1/readings
```

### 8) Get a room by ID
```bash
curl -X GET http://localhost:8080/CSAwebApp/api/v1/rooms/room-101
```

## Notes
- Data is stored in memory (`MockDatabase`) and resets when the server restarts.
- If your app is deployed under a different context path, adjust `CSAwebApp` in URLs accordingly.
