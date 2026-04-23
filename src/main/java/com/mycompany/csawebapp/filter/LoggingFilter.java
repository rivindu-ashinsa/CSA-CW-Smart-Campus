package com.mycompany.csawebapp.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LoggingFilter implements
        ContainerRequestFilter,
        ContainerResponseFilter {

    @Override
    public void filter(
            ContainerRequestContext requestContext)
            throws IOException {

        System.out.println(
                "Incoming: "
                + requestContext.getMethod()
                + " "
                + requestContext.getUriInfo().getPath());
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
            throws IOException {

        System.out.println(
                "Outgoing Status: "
                + responseContext.getStatus());
    }
}