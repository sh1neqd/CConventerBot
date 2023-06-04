package ru.dakon.CConventerBot.exception;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ErrorDTO {
    private String error;
}
