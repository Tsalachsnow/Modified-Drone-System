package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.musalasoft.enums.Status;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BatteryCapacityResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
    private String batteryCapacity;
    private Status batteryStatus;
}
