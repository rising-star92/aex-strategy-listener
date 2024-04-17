package com.walmart.aex.strategy.listener.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ClpApListenerException extends RuntimeException{

    public ClpApListenerException(String message) {

        super(message);
    }

    public ClpApListenerException(String message, Throwable cause) {

        super(message, cause);
    }
}
