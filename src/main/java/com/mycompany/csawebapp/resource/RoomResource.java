/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.csawebapp.resource;

import com.mycompany.csawebapp.dao.MockDatabase;
import com.mycompany.csawebapp.model.Room;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author User
 */

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class RoomResource {
    @GET
    public Collection<Room> getAllRooms(){
        return MockDatabase.rooms.values(); 
    }
    
    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id){
        Room room = MockDatabase.rooms.get(id); 
        if (room == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }
    
    @POST
    public Response createRoom(Room room){
        MockDatabase.rooms.put(room.getId(), room); 
        return Response.status(Response.Status.CREATED).entity(room).build(); 
    }
    
}
