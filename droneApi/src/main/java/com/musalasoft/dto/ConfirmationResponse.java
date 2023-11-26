package com.musalasoft.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class ConfirmationResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
    private String dispatchNumber;
    private LocalTime expectedReturnTime;
    private List<LoadMedicationResponse.MedicationResp> receivedMedications;

}
