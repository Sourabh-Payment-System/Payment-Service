package payment.system.app.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(
            HttpStatus.BAD_REQUEST),
    
    IDEMPOTENCY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND),

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND),

    INSUFFICIENT_BALANCE(
            HttpStatus.BAD_REQUEST),

    PAYMENT_FAILED(
            HttpStatus.BAD_REQUEST),

    PAYMENT_PROCESSING_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR),

    WALLET_SERVICE_ERROR(
            HttpStatus.SERVICE_UNAVAILABLE),

    DATABASE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR),

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR),
	
    WALLET_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR);
    
    
    private final HttpStatus httpStatus;
    
   
}