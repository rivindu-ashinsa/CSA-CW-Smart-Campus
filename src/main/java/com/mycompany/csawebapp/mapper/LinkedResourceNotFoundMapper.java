package com.mycompany.csawebapp.mapper;

import com.mycompany.csawebapp.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(
            LinkedResourceNotFoundException ex) {

        return Response.status(422)
                .entity("{\"error\":\"Linked room not found\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}