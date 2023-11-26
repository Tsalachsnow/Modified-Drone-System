package com.musalasoft.dto;

import lombok.Data;

@Data
public class DroneRegistrationResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
}
