package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;

import payment.system.app.entity.Transaction;

import payment.system.app.enums.PaymentStatus;

import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.exception.ResourceNotFoundException;

import payment.system.app.facade.WalletFacadeService;

import payment.system.app.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository
            transactionRepository;

    private final WalletFacadeService
            walletFacadeService;

    /**
     * Transfer Money
     */
    public TransactionResponse transferMoney(
            TransferRequest request) {

        log.info(
                "Payment transfer initiated: senderUserId={}, receiverUserId={}, amount={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount());

        validateTransferRequest(request);

        String transactionReference =
                generateTransactionReference();

        Transaction transaction =
                createPendingTransaction(
                        request,
                        transactionReference);

        try {

            log.info(
                    "Calling wallet service for transactionRef={}",
                    transactionReference);

            WalletTransferResponse walletResponse =
                    walletFacadeService
                            .transferMoney(request);

            validateWalletResponse(
                    walletResponse,
                    request);

            updateTransactionStatus(
                    transaction.getId(),
                    PaymentStatus.SUCCESS);

            log.info(
                    "Payment transfer completed successfully: transactionRef={}",
                    transactionReference);

            return TransactionResponse.builder()
                    .transactionReference(
                            transactionReference)
                    .senderUserId(
                            walletResponse.getSenderUserId())
                    .receiverUserId(
                            walletResponse.getReceiverUserId())
                    .amount(
                            walletResponse.getAmount())
                    .status(
                            PaymentStatus.SUCCESS.name())
                    .timestamp(
                            LocalDateTime.now())
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Payment transfer failed: transactionRef={}",
                    transactionReference,
                    ex);

            try {

                updateTransactionStatus(
                        transaction.getId(),
                        PaymentStatus.FAILED);

            } catch (Exception statusEx) {

                log.error(
                        "Failed to update transaction status to FAILED: transactionRef={}",
                        transactionReference,
                        statusEx);
            }

            if (ex instanceof BadRequestException) {

                throw ex;
            }

            throw new PaymentProcessingException(
                    "Payment transfer failed");
        }
    }

    /**
     * Create Pending Transaction
     */
    @Transactional
    public Transaction createPendingTransaction(
            TransferRequest request,
            String transactionReference) {

        log.info(
                "Creating pending transaction: transactionRef={}",
                transactionReference);

        try {

            Transaction transaction =
                    Transaction.builder()
                            .senderUserId(
                                    request.getSenderUserId())
                            .receiverUserId(
                                    request.getReceiverUserId())
                            .amount(
                                    request.getAmount())
                            .transactionReference(
                                    transactionReference)
                            .status(
                                    PaymentStatus.PENDING)
                            .createdAt(
                                    LocalDateTime.now())
                            .build();

            Transaction savedTransaction =
                    transactionRepository.save(
                            transaction);

            log.info(
                    "Pending transaction created successfully: transactionId={}, transactionRef={}",
                    savedTransaction.getId(),
                    transactionReference);

            return savedTransaction;

        } catch (Exception ex) {

            log.error(
                    "Failed to create pending transaction: transactionRef={}",
                    transactionReference,
                    ex);

            throw new PaymentProcessingException(
                    "Failed to create payment transaction");
        }
    }

    /**
     * Update Transaction Status
     */
    @Transactional
    public void updateTransactionStatus(
            Long transactionId,
            PaymentStatus status) {

        log.info(
                "Updating transaction status: transactionId={}, status={}",
                transactionId,
                status);

        Transaction transaction =
                transactionRepository.findById(
                        transactionId)
                        .orElseThrow(() -> {

                            log.error(
                                    "Transaction not found: transactionId={}",
                                    transactionId);

                            return new ResourceNotFoundException(
                                    "Transaction not found with id: "
                                            + transactionId);
                        });

        transaction.setStatus(status);

        transactionRepository.save(transaction);

        log.info(
                "Transaction status updated successfully: transactionId={}, status={}",
                transactionId,
                status);
    }

    /**
     * Validate Transfer Request
     */
    private void validateTransferRequest(
            TransferRequest request) {

        log.info(
                "Validating transfer request");

        if (request == null) {

            log.error(
                    "Transfer request is null");

            throw new BadRequestException(
                    "Transfer request cannot be null");
        }

        if (request.getSenderUserId() == null) {

            log.error(
                    "Sender userId is null");

            throw new BadRequestException(
                    "Sender userId is required");
        }

        if (request.getReceiverUserId() == null) {

            log.error(
                    "Receiver userId is null");

            throw new BadRequestException(
                    "Receiver userId is required");
        }

        if (request.getAmount() == null) {

            log.error(
                    "Transfer amount is null");

            throw new BadRequestException(
                    "Transfer amount is required");
        }

        if (request.getAmount()
                .signum() <= 0) {

            log.error(
                    "Invalid transfer amount={}",
                    request.getAmount());

            throw new BadRequestException(
                    "Transfer amount must be greater than zero");
        }

        if (request.getSenderUserId()
                .equals(
                        request.getReceiverUserId())) {

            log.error(
                    "Self transfer attempted for userId={}",
                    request.getSenderUserId());

            throw new BadRequestException(
                    "Sender and receiver cannot be same");
        }

        log.info(
                "Transfer request validated successfully");
    }

    /**
     * Validate Wallet Service Response
     */
    private void validateWalletResponse(
            WalletTransferResponse walletResponse,
            TransferRequest request) {

        log.info(
                "Validating wallet service response");

        if (walletResponse == null) {

            log.error(
                    "Wallet service returned null response");

            throw new PaymentProcessingException(
                    "Wallet service returned null response");
        }

        if (walletResponse.getStatus() == null
                || !walletResponse.getStatus()
                        .equalsIgnoreCase("SUCCESS")) {

            log.error(
                    "Wallet transfer failed with status={}",
                    walletResponse.getStatus());

            throw new PaymentProcessingException(
                    "Wallet transfer failed");
        }

        if (!request.getSenderUserId()
                .equals(
                        walletResponse.getSenderUserId())) {

            log.error(
                    "Sender userId mismatch: request={}, response={}",
                    request.getSenderUserId(),
                    walletResponse.getSenderUserId());

            throw new PaymentProcessingException(
                    "Sender userId mismatch");
        }

        if (!request.getReceiverUserId()
                .equals(
                        walletResponse.getReceiverUserId())) {

            log.error(
                    "Receiver userId mismatch: request={}, response={}",
                    request.getReceiverUserId(),
                    walletResponse.getReceiverUserId());

            throw new PaymentProcessingException(
                    "Receiver userId mismatch");
        }

        if (request.getAmount()
                .compareTo(
                        walletResponse.getAmount()) != 0) {

            log.error(
                    "Transfer amount mismatch: request={}, response={}",
                    request.getAmount(),
                    walletResponse.getAmount());

            throw new PaymentProcessingException(
                    "Transfer amount mismatch");
        }

        if (walletResponse
                .getWalletTransactionReference() == null
                || walletResponse
                        .getWalletTransactionReference()
                        .isBlank()) {

            log.error(
                    "Wallet transaction reference is missing");

            throw new PaymentProcessingException(
                    "Wallet transaction reference is missing");
        }

        log.info(
                "Wallet service response validated successfully");
    }

    /**
     * Generate Transaction Reference
     */
    private String generateTransactionReference() {

        return "TXN-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}