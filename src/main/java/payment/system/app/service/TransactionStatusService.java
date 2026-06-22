package payment.system.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionStatusService {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(
            Long transactionId,
            PaymentStatus newStatus) {

        Transaction transaction =
                transactionRepository.findByIdForUpdate(transactionId)
                        .orElseThrow();

        transaction.setStatus(newStatus);
    }
}
