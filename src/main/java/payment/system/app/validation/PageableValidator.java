package payment.system.app.validation;

import org.springframework.data.domain.Pageable;

import payment.system.app.exception.BadRequestException;

public final class PageableValidator {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableValidator() {
    }

    public static void validate(Pageable pageable) {

        if (pageable == null) {
            throw new BadRequestException(
                    "Page request cannot be null");
        }

        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative");
        }

        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException(
                    "Page size must be greater than zero");
        }

        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BadRequestException(
                    "Maximum allowed page size is "
                            + MAX_PAGE_SIZE);
        }
    }
}