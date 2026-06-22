package payment.system.app.service;

import static payment.system.app.constants.ErrorMessages.TRANSACTION_NOT_FOUND;
import static payment.system.app.constants.ErrorMessages.UNABLE_TO_CREATE_TRANSACTION;
import static payment.system.app.constants.ErrorMessages.UNABLE_TO_UPDATE_TRANSACTION;
import static payment.system.app.constants.LogMessages.TRANSACTION_CREATED;
import static payment.system.app.constants.LogMessages.TRANSACTION_CREATION_FAILED;
import static payment.system.app.constants.LogMessages.TRANSACTION_STATUS_UPDATED;
import static payment.system.app.constants.LogMessages.TRANSACTION_STATUS_UPDATE_FAILED;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.TransferRequest;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.ErrorCode;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.exception.ResourceNotFoundException;
import payment.system.app.repository.TransactionRepository;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionStatusService transactionStatusService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createPendingTransaction(
            TransferRequest request,
            String transactionReference) {

        try {

            log.debug(
                    "Creating pending transaction. transactionReference={}, senderUserId={}, receiverUserId={}, amount={}",
                    transactionReference,
                    request.getSenderUserId(),
                    request.getReceiverUserId(),
                    request.getAmount());

            Transaction transaction =
                    Transaction.builder()
                            .senderUserId(request.getSenderUserId())
                            .receiverUserId(request.getReceiverUserId())
                            .amount(request.getAmount())
                            .transactionReference(transactionReference)
                            .status(PaymentStatus.PENDING)
                            .build();

            Transaction savedTransaction =
                    transactionRepository.save(transaction);

            log.info(
                    TRANSACTION_CREATED,
                    savedTransaction.getId(),
                    transactionReference);

            return savedTransaction;

        } catch (DataIntegrityViolationException ex) {

            log.error(
                    "Duplicate transaction reference detected. transactionReference={}",
                    transactionReference,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    "Unable to generate unique transaction reference");

        } catch (Exception ex) {

            log.error(
                    TRANSACTION_CREATION_FAILED,
                    transactionReference,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    UNABLE_TO_CREATE_TRANSACTION);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTransactionStatus(
            Long transactionId,
            PaymentStatus newStatus) {

        try {

            Transaction transaction =
                    getTransactionByIdForUpdate(transactionId);

            PaymentStatus currentStatus =
                    transaction.getStatus();

            log.debug(
                    "Updating transaction status. transactionId={}, transactionRef={}, currentStatus={}, newStatus={}",
                    transactionId,
                    transaction.getTransactionReference(),
                    currentStatus,
                    newStatus);

            if (currentStatus == newStatus) {

                log.info(
                        "Transaction already in requested status. transactionId={}, status={}",
                        transactionId,
                        newStatus);

                return;
            }

            if (currentStatus == PaymentStatus.SUCCESS) {

                log.warn(
                        "Ignoring status update because transaction already completed. transactionId={}, requestedStatus={}",
                        transactionId,
                        newStatus);

                return;
            }

            transaction.setStatus(newStatus);

            log.info(
                    TRANSACTION_STATUS_UPDATED,
                    transactionId,
                    newStatus);

        } catch (ResourceNotFoundException ex) {

            log.warn(
                    "Transaction not found during status update. transactionId={}",
                    transactionId);

            throw ex;

        } catch (ObjectOptimisticLockingFailureException ex) {

            log.error(
                    "Optimistic lock failure. transactionId={}",
                    transactionId,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    "Concurrent transaction update detected");

        } catch (Exception ex) {

            log.error(
                    TRANSACTION_STATUS_UPDATE_FAILED,
                    transactionId,
                    newStatus,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    UNABLE_TO_UPDATE_TRANSACTION);
        }
    }

    public void safelyUpdateTransactionStatus(
            Long transactionId,
            PaymentStatus status) {

        try {
            transactionStatusService.updateStatus(
                    transactionId,
                    status);
        } catch (Exception ex) {
            log.error("Failed", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTransactionSuccess(
            Long transactionId,
            String walletTransactionReference) {

        try {

            if (walletTransactionReference == null
                    || walletTransactionReference.isBlank()) {

                throw new PaymentProcessingException(
                        ErrorCode.WALLET_RESPONSE_INVALID,
                        "Wallet transaction reference is mandatory");
            }

            Transaction transaction =
                    getTransactionByIdForUpdate(transactionId);

            if (transaction.getStatus() == PaymentStatus.SUCCESS) {

                log.info(
                        "Transaction already marked SUCCESS. transactionId={}, transactionRef={}",
                        transactionId,
                        transaction.getTransactionReference());

                return;
            }

            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setWalletTransactionReference(
                    walletTransactionReference);

            log.info(
                    "Transaction marked SUCCESS. transactionId={}, transactionRef={}, walletTransactionReference={}",
                    transactionId,
                    transaction.getTransactionReference(),
                    walletTransactionReference);

        } catch (ResourceNotFoundException ex) {

            log.warn(
                    "Transaction not found while marking SUCCESS. transactionId={}",
                    transactionId);

            throw ex;

        } catch (ObjectOptimisticLockingFailureException ex) {

            log.error(
                    "Optimistic lock failure while marking SUCCESS. transactionId={}",
                    transactionId,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    "Concurrent transaction update detected");

        } catch (Exception ex) {

            log.error(
                    "Failed to mark transaction SUCCESS. transactionId={}, walletTransactionReference={}",
                    transactionId,
                    walletTransactionReference,
                    ex);

            throw new PaymentProcessingException(
                    ErrorCode.DATABASE_ERROR,
                    UNABLE_TO_UPDATE_TRANSACTION);
        }
    }

    @Transactional
    public Transaction getTransactionByIdForUpdate(
            Long transactionId) {

        return transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> {

                    log.warn(
                            "Transaction not found. transactionId={}",
                            transactionId);

                    return new ResourceNotFoundException(
                            TRANSACTION_NOT_FOUND + transactionId);
                });
    }
}

