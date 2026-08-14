package payment.system.app.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import payment.system.app.exception.BadRequestException;

class PageableValidatorTest {

    @Test
    void shouldValidateValidPageable() {

        Pageable pageable = PageRequest.of(0, 20);

        assertDoesNotThrow(() ->
                PageableValidator.validate(pageable));
    }

    @Test
    void shouldThrowExceptionWhenPageableIsNull() {

        assertThrows(
                BadRequestException.class,
                () -> PageableValidator.validate(null));
    }

    @Test
    void shouldThrowExceptionWhenPageNumberIsNegative() {

        Pageable pageable = PageRequest.of(0, 20).withPage(-1);

        assertThrows(
                BadRequestException.class,
                () -> PageableValidator.validate(pageable));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PageRequest.of(0, 0));
    }

    @Test
    void shouldThrowExceptionWhenPageSizeExceedsLimit() {

        Pageable pageable = PageRequest.of(0, 101);

        assertThrows(
                BadRequestException.class,
                () -> PageableValidator.validate(pageable));
    }

}