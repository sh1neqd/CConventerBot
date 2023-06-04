package ru.dakon.CConventerBot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler({RubParsingException.class})
    public ResponseEntity<ErrorDTO> handleExceptionFromPriceService(Exception ex) {
        return new ResponseEntity<ErrorDTO>(new ErrorDTO(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
    }
}
