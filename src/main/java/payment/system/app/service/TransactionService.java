package payment.system.app.service;

import static payment.system.app.constants.ErrorMessages.TRANSACTION_NOT_FOUND;
import static payment.system.app.constants.ErrorMessages.UNABLE_TO_CREATE_TRANSACTION;
import static payment.system.app.constants.ErrorMessages.UNABLE_TO_UPDATE_TRANSACTION;

import static payment.system.app.constants.LogMessages.TRANSACTION_CREATED;
import static payment.system.app.constants.LogMessages.TRANSACTION_CREATION_FAILED;
import static payment.system.app.constants.LogMessages.TRANSACTION_STATUS_UPDATED;
import static payment.system.app.constants.LogMessages.TRANSACTION_STATUS_UPDATE_FAILED;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import payment.system.app.dto.TransferRequest;

import payment.system.app.entity.Transaction;

import payment.system.app.enums.PaymentStatus;

import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.exception.ResourceNotFoundException;

import payment.system.app.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW)
    public Transaction createPendingTransaction(
            TransferRequest request,
            String transactionReference) {

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
                            .build();

            Transaction savedTransaction =
                    transactionRepository.save(
                            transaction);

            log.info(
                    TRANSACTION_CREATED,
                    savedTransaction.getId(),
                    transactionReference);

            return savedTransaction;

        } catch (Exception ex) {

            log.error(
                    TRANSACTION_CREATION_FAILED,
                    transactionReference,
                    ex);

            throw new PaymentProcessingException(
                    UNABLE_TO_CREATE_TRANSACTION);
        }
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW)
    public void updateTransactionStatus(
            Long transactionId,
            PaymentStatus newStatus) {

        try {

            Transaction transaction =
                    getTransactionById(
                            transactionId);

            PaymentStatus currentStatus =
                    transaction.getStatus();

            if (currentStatus == newStatus) {

                log.info(
                        "Transaction {} already in status {}",
                        transactionId,
                        newStatus);

                return;
            }

            if (currentStatus == PaymentStatus.SUCCESS) {

                log.warn(
                        "Transaction {} already completed",
                        transactionId);

                return;
            }

            transaction.setStatus(
                    newStatus);

            transactionRepository.save(
                    transaction);

            log.info(
                    TRANSACTION_STATUS_UPDATED,
                    transactionId,
                    newStatus);

        } catch (ResourceNotFoundException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error(
                    TRANSACTION_STATUS_UPDATE_FAILED,
                    transactionId,
                    newStatus,
                    ex);

            throw new PaymentProcessingException(
                    UNABLE_TO_UPDATE_TRANSACTION);
        }
    }

    public void safelyUpdateTransactionStatus(
            Long transactionId,
            PaymentStatus status) {

        try {

            updateTransactionStatus(
                    transactionId,
                    status);

        } catch (Exception ex) {

            log.error(
                    "Failed to update transaction status. transactionId={}, status={}",
                    transactionId,
                    status,
                    ex);
        }
    }
    @Transactional(
            propagation = Propagation.REQUIRES_NEW)
    public void markTransactionSuccess(
            Long transactionId,
            String walletTransactionReference) {

        try {

            Transaction transaction =
                    getTransactionById(
                            transactionId);

            if (transaction.getStatus()
                    == PaymentStatus.SUCCESS) {

                log.info(
                        "Transaction already SUCCESS. transactionId={}",
                        transactionId);

                return;
            }

            transaction.setStatus(
                    PaymentStatus.SUCCESS);

            transaction.setWalletTransactionReference(
                    walletTransactionReference);

            transactionRepository.save(
                    transaction);

            log.info(
                    "Transaction marked SUCCESS. transactionId={}, walletReference={}",
                    transactionId,
                    walletTransactionReference);

        } catch (Exception ex) {

            log.error(
                    "Failed to mark transaction SUCCESS. transactionId={}",
                    transactionId,
                    ex);

            throw new PaymentProcessingException(
                    UNABLE_TO_UPDATE_TRANSACTION);
        }
    }

    public Transaction getTransactionById(
            Long transactionId) {

        return transactionRepository.findById(
                        transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TRANSACTION_NOT_FOUND
                                        + transactionId));
    }

    public boolean existsByTransactionReference(
            String transactionReference) {

        return transactionRepository
                .existsByTransactionReference(
                        transactionReference);
    }
}