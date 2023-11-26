package com.musalasoft.util.exception;

import lombok.Data;

@Data
public class NoDataFoundException extends RuntimeException{


    public NoDataFoundException(String message){
        super(message);
    }


}
