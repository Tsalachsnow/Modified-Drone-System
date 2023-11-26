package com.musalasoft.util.exception;

/* @author prosperamalaha */

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class GenericException extends RuntimeException {

    private HttpStatus httpStatus;
    private String code;
    private String message;

    public GenericException(String code, String message, HttpStatus httpStatus){
        super(message);
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
