package com.mycompany.csawebapp.resource;

import com.mycompany.csawebapp.dao.MockDatabase;
import com.mycompany.csawebapp.exception.SensorUnavailableException;
import com.mycompany.csawebapp.model.Sensor;
import com.mycompany.csawebapp.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {

        List<SensorReading> list
                = MockDatabase.readings.get(sensorId);

        if (list == null) {
            list = new ArrayList<>();
        }

        return Response.ok(list).build();
    }

    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor
                = MockDatabase.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException();

        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        List<SensorReading> list
                = MockDatabase.readings.get(sensorId);

        if (list == null) {
            list = new ArrayList<>();
            MockDatabase.readings.put(sensorId, list);
        }

        list.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
