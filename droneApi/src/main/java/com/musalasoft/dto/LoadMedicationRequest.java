package com.musalasoft.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Accessors(chain = true)
public class LoadMedicationRequest{

@Data
@Accessors(chain = true)
public static class Request {
    @Size(min = 4, max = 100, message
            = "Serial_Number must be between 4 and 100 characters")
    private String serialNumber;
    private List<Medication1> medications;
}
    @Data
    @Accessors(chain = true)
    public static class Medication1{

        @Pattern(regexp ="^[a-zA-Z0-9_-]*$")
        private String name;


        private double weight;

        @Pattern(regexp = "^[A-Z0-9_]*$")
        private String code;

        private String image;
    }
}
