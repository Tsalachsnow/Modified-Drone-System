package com.musalasoft.util.exception.controller;

import com.musalasoft.util.ResponseCodes;
import com.musalasoft.util.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler1 {



    /**
     * Handles method validation exceptions
     * @param ex: the exception
     * @return the response entity
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    protected ResponseEntity<ErrorResponseDTO> handleMethodValidations(MethodArgumentNotValidException ex){

        String errors = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(" | "));

        return ResponseEntity.badRequest().body(new ErrorResponseDTO("E404", errors));
    }

    /**
     * Handles constraint validation exceptions
     * @param ex: the exception
     * @return the response entity
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<ErrorResponseDTO> handleConstraintValidations(ConstraintViolationException ex){

        String errors = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(" | "));


        return ResponseEntity.badRequest().body(new ErrorResponseDTO("E21", errors));
    }


    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handleAlreadyExistsException(AlreadyExistException ex,HttpServletRequest request){
        log.error("Resource not found exception occurred while trying to process request: {}",request.getRequestURI());
        return ResponseEntity.badRequest().body(new ErrorResponseDTO("E45",ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFoundException(HttpServletRequest request,ResourceNotFoundException ex){
        log.error("Resource not found exception occurred while trying to process request: {}",request.getRequestURI());
        ResponseEntity<ErrorResponseDTO> response = new ResponseEntity<>(new ErrorResponseDTO("E90",ex.getMessage()), HttpStatus.NOT_FOUND);
        return response;
    }

    @ExceptionHandler(Exception.class)

    public ResponseEntity<ErrorResponseDTO> handle(Exception ex,
                                         HttpServletRequest request, HttpServletResponse response) {
        log.error("Exception occurred while processing the request: {} because: {}",request.getRequestURI(),ex.getMessage(),ex);
        return  ResponseEntity.internalServerError().body((new ErrorResponseDTO("E91","An unknown exception just occurred. Please try again or contact administrator")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)

    public ResponseEntity<ErrorResponseDTO> handleModelError(Exception ex,
                                                   HttpServletRequest request, HttpServletResponse response) {
        log.error("Exception occurred while processing the request: {} because: {}",request.getRequestURI(),ex.getMessage(),ex);
        return  ResponseEntity.internalServerError().body((new ErrorResponseDTO(ResponseCodes.BAD_INPUT_DATA,ResponseCodes.getErrorMessage(ResponseCodes.BAD_INPUT_DATA))));
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(CustomAuthenticationException ex){
        log.error("Exception occurred while trying to authenticate user because: {}",ex.getMessage());
        return  new ResponseEntity<>(new ErrorResponseDTO("E46",ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(HttpServletRequest request,ValidationException ex){
        log.error("validation exception occurred while trying to process request: {} because: {}",request.getRequestURI(),ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponseDTO("E98",ex.getMessage()));

    }

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoDataFoundException(HttpServletRequest request, NoDataFoundException ex){
        log.error("validation exception occurred while trying to process request: {} because: {}",request.getRequestURI(),ex.getMessage());
        return new ResponseEntity<>(new ErrorResponseDTO("E90",ex.getMessage()),HttpStatus.NO_CONTENT);
    }


    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(HttpServletRequest request, GenericException ex){
        log.error("generic exception occurred while trying to process request: {} because: {}",request.getRequestURI(),ex.getMessage());

        return ResponseEntity.status((ex.getHttpStatus() == null) ? HttpStatus.INTERNAL_SERVER_ERROR : ex.getHttpStatus()).body(new ErrorResponseDTO(ex.getCode(), (ex.getMessage() == null) ? ex.getCode() : ex.getMessage()));
    }

}
