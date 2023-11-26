package com.musalasoft.dto;

import lombok.Data;

@Data
public class ChargeBatteryResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
    private String status;
}
