package com.musalasoft.dto;

import com.musalasoft.enums.State;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DeliveryStatusResponse {
    private String responseCode;
    private String responseMessage;
    private LocalTime expectedDeliveryTime;
    private State deliveryStatus;

}
