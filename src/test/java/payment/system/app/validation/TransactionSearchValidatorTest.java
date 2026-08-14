package payment.system.app.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.exception.BadRequestException;

class TransactionSearchValidatorTest {

    @Test
    void shouldValidateRequestSuccessfully() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .minAmount(BigDecimal.valueOf(100))
                        .maxAmount(BigDecimal.valueOf(500))
                        .from(LocalDateTime.now().minusDays(2))
                        .to(LocalDateTime.now())
                        .build();

        assertDoesNotThrow(() ->
                TransactionSearchValidator.validate(request));
    }

    @Test
    void shouldThrowExceptionWhenMinAmountGreaterThanMaxAmount() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .minAmount(BigDecimal.valueOf(500))
                        .maxAmount(BigDecimal.valueOf(100))
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> TransactionSearchValidator.validate(request));
    }

    @Test
    void shouldThrowExceptionWhenFromDateAfterToDate() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .from(LocalDateTime.now())
                        .to(LocalDateTime.now().minusDays(1))
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> TransactionSearchValidator.validate(request));
    }

    @Test
    void shouldAllowNullRequest() {

        assertDoesNotThrow(() ->
                TransactionSearchValidator.validate(null));
    }

}