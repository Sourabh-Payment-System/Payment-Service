package payment.system.app.service;

import static payment.system.app.constants.ErrorMessages.PAYMENT_TRANSFER_FAILED_MESSAGE;
import static payment.system.app.constants.ErrorMessages.RECEIVER_USERID_MISMATCH;
import static payment.system.app.constants.ErrorMessages.SENDER_RECEIVER_SAME;
import static payment.system.app.constants.ErrorMessages.SENDER_USERID_MISMATCH;
import static payment.system.app.constants.ErrorMessages.TRANSFER_AMOUNT_MISMATCH;
import static payment.system.app.constants.ErrorMessages.WALLET_RESPONSE_NULL;
import static payment.system.app.constants.ErrorMessages.WALLET_TRANSFER_FAILED;
import static payment.system.app.constants.LogMessages.PAYMENT_TRANSFER_FAILED_LOG;
import static payment.system.app.constants.LogMessages.PAYMENT_TRANSFER_INITIATED;
import static payment.system.app.constants.LogMessages.PAYMENT_TRANSFER_SUCCESS;
import static payment.system.app.constants.LogMessages.SAME_USER_TRANSFER_ATTEMPT;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_PREFIX;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_REFERENCE_LENGTH;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.facade.WalletFacadeService;
import payment.system.app.mapper.TransactionMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final WalletFacadeService walletFacadeService;
    private final TransactionMapper transactionMapper;
    private final TransactionService transactionService;

    public TransactionResponse transferMoney(
            TransferRequest request) {

        validateBusinessRules(request);

        String transactionReference =
                generateTransactionReference();

        log.info(
                "Payment initiated. transactionRef={}, senderUserId={}, receiverUserId={}, amount={}",
                transactionReference,
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount());

        Transaction transaction =
                transactionService.createPendingTransaction(
                        request,
                        transactionReference);

        try {

            WalletTransferResponse walletResponse =
                    walletFacadeService.transferMoney(
                            request);

            validateWalletResponse(
                    walletResponse,
                    request);

            transactionService.markTransactionSuccess(
                    transaction.getId(),
                    walletResponse.getWalletTransactionReference());

            log.info(
                    "Payment completed successfully. transactionRef={}, walletTxnRef={}",
                    transactionReference,
                    walletResponse.getWalletTransactionReference());

            return transactionMapper.toResponse(
                    transactionReference,
                    walletResponse,
                    PaymentStatus.SUCCESS);
        }

        catch (BadRequestException ex) {

        	transactionService.safelyUpdateTransactionStatus(
                    transaction.getId(),PaymentStatus.FAILED);;

            log.error(
                    "Business validation failed. transactionRef={}, message={}",
                    transactionReference,
                    ex.getMessage());

            throw ex;
        }

        catch (PaymentProcessingException ex) {

        	transactionService.safelyUpdateTransactionStatus(
                    transaction.getId(),PaymentStatus.FAILED);

            log.error(
                    "Payment processing failed. transactionRef={}, message={}",
                    transactionReference,
                    ex.getMessage(),
                    ex);

            throw ex;
        }

        catch (Exception ex) {

        	transactionService.safelyUpdateTransactionStatus(
                    transaction.getId(),PaymentStatus.FAILED);;

            log.error(
                    "Unexpected payment failure. transactionRef={}",
                    transactionReference,
                    ex);

            throw new PaymentProcessingException(
                    PAYMENT_TRANSFER_FAILED_MESSAGE);
        }
    }
    private void validateBusinessRules(
            TransferRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Transfer request cannot be null");
        }

        if (request.getSenderUserId() == null) {
            throw new BadRequestException(
                    "Sender user id is mandatory");
        }

        if (request.getReceiverUserId() == null) {
            throw new BadRequestException(
                    "Receiver user id is mandatory");
        }

        if (request.getAmount() == null) {
            throw new BadRequestException(
                    "Amount is mandatory");
        }

        if (request.getAmount().signum() <= 0) {
            throw new BadRequestException(
                    "Amount must be greater than zero");
        }

        if (request.getSenderUserId()
                .equals(request.getReceiverUserId())) {

            log.error(
                    SAME_USER_TRANSFER_ATTEMPT,
                    request.getSenderUserId());

            throw new BadRequestException(
                    SENDER_RECEIVER_SAME);
        }
    }

    private void validateWalletResponse(
            WalletTransferResponse walletResponse,
            TransferRequest request) {

        if (walletResponse == null) {

            throw new PaymentProcessingException(
                    WALLET_RESPONSE_NULL);
        }

        if (walletResponse.getWalletTransactionReference() == null
                || walletResponse.getWalletTransactionReference()
                        .isBlank()) {

            throw new PaymentProcessingException(
                    "Wallet transaction reference missing");
        }

        if (walletResponse.getStatus() == null) {

            throw new PaymentProcessingException(
                    "Wallet status missing");
        }

        if (walletResponse.getSenderUserId() == null) {

            throw new PaymentProcessingException(
                    "Sender user id missing");
        }

        if (walletResponse.getReceiverUserId() == null) {

            throw new PaymentProcessingException(
                    "Receiver user id missing");
        }

        if (walletResponse.getAmount() == null) {

            throw new PaymentProcessingException(
                    "Transferred amount missing");
        }

        if (walletResponse.getSenderBalance() == null) {

            throw new PaymentProcessingException(
                    "Sender balance missing");
        }

        if (walletResponse.getReceiverBalance() == null) {

            throw new PaymentProcessingException(
                    "Receiver balance missing");
        }

        if (walletResponse.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new PaymentProcessingException(
                    WALLET_TRANSFER_FAILED);
        }

        if (!request.getSenderUserId()
                .equals(walletResponse.getSenderUserId())) {

            throw new PaymentProcessingException(
                    SENDER_USERID_MISMATCH);
        }

        if (!request.getReceiverUserId()
                .equals(walletResponse.getReceiverUserId())) {

            throw new PaymentProcessingException(
                    RECEIVER_USERID_MISMATCH);
        }

        if (request.getAmount()
                .compareTo(walletResponse.getAmount()) != 0) {

            throw new PaymentProcessingException(
                    TRANSFER_AMOUNT_MISMATCH);
        }
    }

    private String generateTransactionReference() {

        String reference;

        do {

            reference =
                    TRANSACTION_PREFIX
                            + UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(
                                            0,
                                            TRANSACTION_REFERENCE_LENGTH)
                                    .toUpperCase();

        } while (
                transactionService
                        .existsByTransactionReference(
                                reference));

        log.debug(
                "Generated transaction reference={}",
                reference);

        return reference;
    }
}