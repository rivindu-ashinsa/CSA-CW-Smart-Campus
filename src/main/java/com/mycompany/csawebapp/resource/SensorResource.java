/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.csawebapp.resource;

import com.mycompany.csawebapp.dao.MockDatabase;
import com.mycompany.csawebapp.model.Sensor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author User
 */

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    
    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type){
        if (type == null || type.trim().isEmpty()){
            return MockDatabase.sensors.values(); 
        }
        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : MockDatabase.sensors.values()){
            if (sensor.getType().equalsIgnoreCase(type)){
                filtered.add(sensor); 
            }
        }
        return filtered; 
    }
}
