package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.TransferRequest;
import payment.system.app.entity.Transaction;
import payment.system.app.entity.Wallet;
import payment.system.app.exception.InsufficientBalanceException;
import payment.system.app.exception.ResourceNotFoundException;
import payment.system.app.repository.TransactionRepository;
import payment.system.app.repository.WalletRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transferMoney(TransferRequest request) {

        Wallet senderWallet = walletRepository.findByUserId(request.getSenderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Sender wallet not found"));

        Wallet receiverWallet = walletRepository.findByUserId(request.getReceiverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Receiver wallet not found"));

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Deduct sender balance
        senderWallet.setBalance(
                senderWallet.getBalance().subtract(request.getAmount())
        );

        // Credit receiver balance
        receiverWallet.setBalance(
                receiverWallet.getBalance().add(request.getAmount())
        );

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction transaction = Transaction.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .transactionRef(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }
}