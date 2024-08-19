package com.giftAndGo.assignment.web.controller.advice;

import com.giftAndGo.assignment.domain.exception.InputProcessingException;
import com.giftAndGo.assignment.domain.exception.InvalidInputException;
import com.giftAndGo.assignment.domain.exception.IpBlockedException;
import com.giftAndGo.assignment.web.model.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({InputProcessingException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final ResponseEntity<ErrorResponse> handleDomainException(InputProcessingException e){
        log.error(e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler({InvalidInputException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public final ResponseEntity<ErrorResponse> handleBadRequest(InvalidInputException e){
        log.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(ErrorResponse.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e){
        log.error(e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder().message("There was an unexpected error while processing your request").build());
    }

    @ExceptionHandler(IpBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleIpBlockedException(IpBlockedException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder().message(e.getMessage()).build());
    }
}
