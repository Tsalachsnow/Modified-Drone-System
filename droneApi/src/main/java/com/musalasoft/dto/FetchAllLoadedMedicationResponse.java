package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.musalasoft.models.Medication;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FetchAllLoadedMedicationResponse {
    private String responseCode;
    private String responseMessage;
    private List<Medication> medicationList;
}
