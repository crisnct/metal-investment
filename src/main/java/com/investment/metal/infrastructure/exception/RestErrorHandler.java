package com.investment.metal.infrastructure.exception;

import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Infrastructure exception handler for REST endpoints.
 * This class handles exceptions at the infrastructure layer and converts them to HTTP responses.
 * Follows Clean Architecture principles by separating infrastructure concerns.
 */
@ControllerAdvice
public class RestErrorHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object processValidationError(BusinessException ex) {
        return new ExceptionDto(ex.getStatusCode(), ex.getMessage());
    }
}
