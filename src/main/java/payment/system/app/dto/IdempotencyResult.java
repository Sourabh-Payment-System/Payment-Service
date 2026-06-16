package payment.system.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import payment.system.app.entity.IdempotencyRecord;

@Getter
@AllArgsConstructor
public class IdempotencyResult {

    private final IdempotencyRecord record;
    private final boolean ownedByCurrentRequest;
}
