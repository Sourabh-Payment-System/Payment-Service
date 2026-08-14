package payment.system.app.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import payment.system.app.dto.PageResponse;

class PageResponseUtilTest {

    @Test
    void shouldConvertPageToPageResponse() {

        Page<String> page =
                new PageImpl<>(
                        List.of("A", "B", "C"),
                        PageRequest.of(0, 3),
                        3);

        PageResponse<String> response =
                PageResponseUtil.from(page);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(3);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void shouldReturnEmptyResponseWhenPageIsNull() {

        PageResponse<String> response =
                PageResponseUtil.from(null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNull();
    }

    @Test
    void shouldConvertEmptyPage() {

        Page<String> page =
                Page.empty();

        PageResponse<String> response =
                PageResponseUtil.from(page);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.isEmpty()).isTrue();
    }

}