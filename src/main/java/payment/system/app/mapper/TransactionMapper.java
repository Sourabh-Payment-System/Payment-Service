package payment.system.app.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.PaymentStatus;

@Component
public class TransactionMapper {

    /**
     * Maps Wallet Service response to API response.
     */
    public TransactionResponse toResponse(
            String transactionReference,
            WalletTransferResponse walletResponse,
            PaymentStatus status) {

        return TransactionResponse.builder()
                .transactionReference(transactionReference)
                .walletTransactionReference(
                        walletResponse.getWalletTransactionReference())
                .senderUserId(
                        walletResponse.getSenderUserId())
                .receiverUserId(
                        walletResponse.getReceiverUserId())
                .amount(
                        walletResponse.getAmount())
                .status(
                        status.name())
                .timestamp(
                        LocalDateTime.now())
                .build();
    }

    /**
     * Maps Transaction entity to API response.
     */
    public TransactionResponse toResponse(
            Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .transactionReference(
                        transaction.getTransactionReference())
                .walletTransactionReference(
                        transaction.getWalletTransactionReference())
                .senderUserId(
                        transaction.getSenderUserId())
                .receiverUserId(
                        transaction.getReceiverUserId())
                .amount(
                        transaction.getAmount())
                .status(
                        transaction.getStatus().name())
                .timestamp(
                        transaction.getCreatedAt())
                .build();
    }

}