package com.musalasoft.dto;

import com.musalasoft.enums.State;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DispatchResponse {
    private String responseCode;
    private String responseMessage;
    private String dispatchNumber;
    private State currentState;
    private LocalTime expectedDeliveryTime;

}
