package io.github.tuanhiep.ltsjavalab.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiErrors {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail bodyValidation(MethodArgumentNotValidException exception) {
        return validationProblem();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail methodValidation(HandlerMethodValidationException exception) {
        return validationProblem();
    }

    private ProblemDetail validationProblem() {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setType(URI.create("urn:lts-java-lab:validation"));
        detail.setTitle("Request validation failed");
        detail.setDetail("One or more request parameters are invalid");
        return detail;
    }
}
