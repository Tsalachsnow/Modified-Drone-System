package com.musalasoft.dto;

import com.musalasoft.enums.Model;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import lombok.Data;

@Data
public class DroneCurrentStatusResponse {
    private String responseCode;
    private String responseMessage;
    private Model droneModel;
    private String batteryCapacity;
    private Status batteryStatus;
    private State droneCurrentState;
}
