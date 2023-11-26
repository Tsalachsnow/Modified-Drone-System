package com.musalasoft.dto;

import lombok.Data;

import java.util.List;

@Data
public class AvailableDronesResponse {
    private String responseCode;
    private String ResponseMessage;
    private List<DroneDto> drones;
}
