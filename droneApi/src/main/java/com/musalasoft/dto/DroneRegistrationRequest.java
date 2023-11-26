package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musalasoft.enums.Model;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

@Validated
@Data
public class DroneRegistrationRequest {
    @Size(min = 4, max = 100, message
            = "Serial_Number must be between 4 and 100 characters")
    private String serialNumber;

    private Model model;

    @JsonIgnore
    @Max(value = 500, message = "weight limit exceeded")
    private int weightLimit;

    @Max(value = 100, message = "Battery Capacity cannot exceed 100%")
    private int batteryCapacity;

    @JsonIgnore
    private Status status;

    @JsonIgnore
    private State state;
}
