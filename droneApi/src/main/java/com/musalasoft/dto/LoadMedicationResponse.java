package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;

@Data
public class LoadMedicationResponse {

    @Data
    @Accessors(chain = true)
    public static class Response{
        private String responseCode;
        private String responseMessage;
        @JsonProperty("loaded_drones_serial_number")
        private String serialNumber;
    }

    @Data
    @Accessors(chain = true)
    public static class MedicationResp{

        @Pattern(regexp ="^[a-zA-Z0-9_-]*$")
        private String name;


        private String weight;

        @Pattern(regexp = "^[A-Z0-9_]*$")
        private String code;

        private String image;
    }
}
