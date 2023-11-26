package com.musalasoft.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class DeliveryConfirmationRequest {
    private String serialNumber;
    private String dispatchNumber;
}
