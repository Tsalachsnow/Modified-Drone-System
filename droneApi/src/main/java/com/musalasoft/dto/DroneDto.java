package com.musalasoft.dto;

import com.musalasoft.enums.Model;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Accessors(chain = true)
public class DroneDto {

    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private Model model;

    private String weightLimit;

    private String weightLimitLeft;

    private String batteryCapacity;

    @Enumerated(EnumType.STRING)
    private Status batteryStatus;

    @Enumerated(EnumType.STRING)
    private State state;
}
