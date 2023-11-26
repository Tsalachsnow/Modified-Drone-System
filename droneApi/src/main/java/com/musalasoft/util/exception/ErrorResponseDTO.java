package com.musalasoft.util.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponseDTO {

    private String message;
    private String responseCode;

    public ErrorResponseDTO(String responseCode, String errorMessage){
        this.responseCode = responseCode;
        this.message=errorMessage;
    }
}
