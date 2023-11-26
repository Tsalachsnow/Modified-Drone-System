package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalTime;

@Data
public class DispatchRequest {
    @Size(min = 4, max = 100, message
            = "Serial_Number must be between 4 and 100 characters")
    private String serialNumber;

    @JsonFormat(pattern = "HH:mm:ss")
    private String estimatedTimeOfDelivery;
}
