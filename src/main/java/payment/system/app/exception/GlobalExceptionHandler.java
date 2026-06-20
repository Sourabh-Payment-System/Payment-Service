package payment.system.app.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.ErrorResponse;
import payment.system.app.enums.ErrorCode;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
            BaseApplicationException ex,
            HttpServletRequest request) {

        log.error(
                "Application exception occurred. path={}, errorCode={}, message={}",
                request.getRequestURI(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {

                	if (error instanceof FieldError fieldError) {

                	    validationErrors.put(
                	            fieldError.getField(),
                	            fieldError.getDefaultMessage());

                	} else {

                	    validationErrors.put(
                	            error.getObjectName(),
                	            error.getDefaultMessage());
                	}
                });
        
        log.warn(
                "Validation failed. path={}, errors={}",
                request.getRequestURI(),
                validationErrors);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Input validation failed",
                request.getRequestURI(),
                validationErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new HashMap<>();

        ex.getConstraintViolations()
                .forEach(violation ->
                        validationErrors.put(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                request.getRequestURI(),
                validationErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "Required request parameter '"
                        + ex.getParameterName()
                        + "' is missing",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "Invalid value for parameter: "
                        + ex.getName(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBodyException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "Request body is invalid or malformed",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED,
                "HTTP method not supported: "
                        + ex.getMethod(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            DataAccessException ex,
            HttpServletRequest request) {

        log.error(
                "Database exception occurred. path={}",
                request.getRequestURI(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.DATABASE_ERROR,
                "Database operation failed",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception occurred. path={}",
                request.getRequestURI(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.",
                request.getRequestURI(),
                null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ErrorResponse errorResponse =
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .errorCode(errorCode.name())
                        .message(message)
                        .path(path)
                        .validationErrors(validationErrors)
                        .build();

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}