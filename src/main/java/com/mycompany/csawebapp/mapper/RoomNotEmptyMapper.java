package com.mycompany.csawebapp.mapper;

import com.mycompany.csawebapp.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {

        String json =
                "{\"error\":\"Room contains sensors\"}";

        return Response.status(Response.Status.CONFLICT)
                .entity(json)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}