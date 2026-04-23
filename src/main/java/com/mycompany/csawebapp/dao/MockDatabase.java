/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.csawebapp.dao;

/**
 *
 * @author User
 */

import com.mycompany.csawebapp.model.Room;
import com.mycompany.csawebapp.model.Sensor;
import com.mycompany.csawebapp.model.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDatabase {

    public static Map<String, Room> rooms = new HashMap<>();

    public static Map<String, Sensor> sensors = new HashMap<>();

    public static Map<String, List<SensorReading>> readings = new HashMap<>();
}
