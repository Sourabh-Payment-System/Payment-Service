package payment.system.app.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransactionSearchRequest;

public interface TransactionQueryService {

    Page<TransactionResponse> searchTransactions(
            TransactionSearchRequest request,
            Pageable pageable);

}