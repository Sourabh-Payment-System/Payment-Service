package payment.system.app.dto;

import java.time.LocalDateTime;
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

    /**
     * Error timestamp
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * Error type
     */
    private String error;

    /**
     * Error message
     */
    private String message;

    /**
     * API path
     */
    private String path;

    /**
     * Validation errors
     */
    private Map<String, String> validationErrors;
}