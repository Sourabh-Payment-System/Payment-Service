package payment.system.app.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

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

import jakarta.validation.ConstraintViolationException;

import payment.system.app.dto.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Resource Not Found Exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.error(
                "Resource not found exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Bad Request Exception
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse>
    handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {

        log.error(
                "Bad request exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Payment Processing Exception
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse>
    handlePaymentProcessingException(
            PaymentProcessingException ex,
            HttpServletRequest request) {

        log.error(
                "Payment processing exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage());

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "PAYMENT_PROCESSING_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Illegal Argument Exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.error(
                "Illegal argument exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Illegal State Exception
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error(
                "Illegal state exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ILLEGAL_STATE",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Validation Exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.error(
                "Validation exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        Map<String, String> validationErrors =
                new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {

                    String fieldName =
                            ((FieldError) error)
                                    .getField();

                    String errorMessage =
                            error.getDefaultMessage();

                    validationErrors.put(
                            fieldName,
                            errorMessage);
                });

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Input validation failed",
                request.getRequestURI(),
                validationErrors);
    }

    /**
     * Handle Constraint Violation Exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.error(
                "Constraint violation exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        Map<String, String> validationErrors =
                new HashMap<>();

        ex.getConstraintViolations()
                .forEach(violation ->
                        validationErrors.put(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "Validation failed",
                request.getRequestURI(),
                validationErrors);
    }

    /**
     * Handle Missing Request Parameter Exception
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse>
    handleMissingRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.error(
                "Missing request parameter exception occurred: path={}, parameter={}",
                request.getRequestURI(),
                ex.getParameterName(),
                ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_REQUEST_PARAMETER",
                "Required request parameter '"
                        + ex.getParameterName()
                        + "' is missing",
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Method Argument Type Mismatch Exception
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.error(
                "Method argument type mismatch exception occurred: path={}, parameter={}",
                request.getRequestURI(),
                ex.getName(),
                ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "Invalid value for parameter: "
                        + ex.getName(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Invalid JSON Request Exception
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.error(
                "Invalid request body exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "Request body is invalid or malformed",
                request.getRequestURI(),
                null);
    }

    /**
     * Handle HTTP Method Not Supported Exception
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse>
    handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.error(
                "HTTP method not supported exception occurred: path={}, method={}",
                request.getRequestURI(),
                ex.getMethod(),
                ex);

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "HTTP method not supported: "
                        + ex.getMethod(),
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Database Exception
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse>
    handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request) {

        log.error(
                "Database exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATABASE_ERROR",
                "Database operation failed",
                request.getRequestURI(),
                null);
    }

    /**
     * Handle Generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception occurred: path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.",
                request.getRequestURI(),
                null);
    }

    /**
     * Common Error Response Builder
     */
    private ResponseEntity<ErrorResponse>
    buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ErrorResponse errorResponse =
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .validationErrors(validationErrors)
                        .path(path)
                        .build();

        return new ResponseEntity<>(
                errorResponse,
                status);
    }
    @ExceptionHandler(WalletServiceException.class)
    public ResponseEntity<ErrorResponse>
    handleWalletServiceException(
            WalletServiceException ex,
            HttpServletRequest request) {

        log.error(
                "Wallet service exception occurred: path={}, status={}, message={}",
                request.getRequestURI(),
                ex.getStatusCode(),
                ex.getMessage());

        return buildErrorResponse(
                HttpStatus.valueOf(
                        ex.getStatusCode()),
                "WALLET_SERVICE_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }
}