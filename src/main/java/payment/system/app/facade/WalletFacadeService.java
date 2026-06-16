package payment.system.app.facade;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.enums.ErrorCode;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.PaymentProcessingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletFacadeService {

    private final WalletRetryService walletRetryService;

    public WalletTransferResponse transferMoney(
            TransferRequest request) {

        log.info(
                "Initiating wallet transfer");

        WalletTransferResponse response =
                walletRetryService.doTransfer(
                        request);

        validateWalletTransferResponse(
                response,
                request);

        log.info(
                "Wallet transfer validated successfully. walletTxnRef={}, status={}",
                response.getWalletTransactionReference(),
                response.getStatus());

        return response;
    }

    private void validateWalletTransferResponse(
            WalletTransferResponse response,
            TransferRequest request) {

        if (response == null) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Wallet response is null");
        }

        validateTransactionReference(
                response);

        validateStatus(
                response);

        validateMandatoryFields(
                response);

        validateResponseConsistency(
                response,
                request);
    }

    private void validateTransactionReference(
            WalletTransferResponse response) {

        if (!StringUtils.hasText(
                response.getWalletTransactionReference())) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Wallet transaction reference missing");
        }
    }

    private void validateStatus(
            WalletTransferResponse response) {

        if (response.getStatus() == null) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Wallet status missing");
        }

        if (response.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new PaymentProcessingException(
                    ErrorCode.PAYMENT_FAILED,
                    "Wallet transfer failed");
        }
    }

    private void validateMandatoryFields(
            WalletTransferResponse response) {

        validateField(
                response.getSenderUserId(),
                "senderUserId");

        validateField(
                response.getReceiverUserId(),
                "receiverUserId");

        validateField(
                response.getAmount(),
                "amount");

        validateField(
                response.getSenderBalance(),
                "senderBalance");

        validateField(
                response.getReceiverBalance(),
                "receiverBalance");
    }

    private void validateField(
            Object value,
            String fieldName) {

        if (value == null) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Wallet response field '" + fieldName + "' is missing");
        }
    }

    private void validateResponseConsistency(
            WalletTransferResponse response,
            TransferRequest request) {

        if (!request.getSenderUserId()
                .equals(
                        response.getSenderUserId())) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Sender user mismatch");
        }

        if (!request.getReceiverUserId()
                .equals(
                        response.getReceiverUserId())) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Receiver user mismatch");
        }

        if (request.getAmount()
                .compareTo(
                        response.getAmount()) != 0) {

            throw new PaymentProcessingException(
                    ErrorCode.WALLET_RESPONSE_INVALID,
                    "Transfer amount mismatch");
        }
    }
}