package com.mycompany.csawebapp.mapper;

import com.mycompany.csawebapp.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(
            SensorUnavailableException ex) {

        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"Sensor unavailable\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}