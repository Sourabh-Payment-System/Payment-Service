package payment.system.app.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String errorCode;

    private String message;

    private String path;

    private String method;

    private String correlationId;

    @Builder.Default
    private Map<String, String> validationErrors =
            Collections.emptyMap();
}