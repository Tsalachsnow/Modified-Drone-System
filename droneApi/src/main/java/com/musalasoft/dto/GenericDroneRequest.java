package com.musalasoft.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class GenericDroneRequest {
    @Size(min = 4, max = 100, message
            = "Serial_Number must be between 4 and 100 characters")
    private String serialNumber;
}
