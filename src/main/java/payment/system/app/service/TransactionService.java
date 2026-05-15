package payment.system.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.entity.Transaction;
import payment.system.app.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(Long userId) {

        return transactionRepository
                .findBySenderIdOrReceiverId(userId, userId);
    }
}