package payment.system.app.util;

import org.springframework.data.domain.Page;

import payment.system.app.dto.PageResponse;

public final class PageResponseUtil {

    private PageResponseUtil() {
    }

    public static <T> PageResponse<T> from(Page<T> page) {

        if (page == null) {
            return PageResponse.<T>builder().build();
        }

        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}