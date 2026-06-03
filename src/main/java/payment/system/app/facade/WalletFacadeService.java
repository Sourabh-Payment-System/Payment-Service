package payment.system.app.facade;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletFacadeService {

    private final WalletRetryService walletRetryService;

    public WalletTransferResponse transferMoney(
            TransferRequest request) {

        try {

            WalletTransferResponse response =
                    walletRetryService.doTransfer(request);

            validateWalletTransferResponse(response);

            return response;
        }

        catch (BadRequestException ex) {

            log.error(
                    "Wallet validation/business error={}",
                    ex.getMessage());

            throw ex;
        }
    }
    private void validateWalletTransferResponse(
            WalletTransferResponse response) {

        if (response == null) {

            throw new PaymentProcessingException(
                    "Wallet response is null");
        }

        if (response.getWalletTransactionReference() == null
                || response.getWalletTransactionReference().isBlank()) {

            throw new PaymentProcessingException(
                    "Wallet transaction reference missing");
        }

        if (response.getStatus() == null) {

            throw new PaymentProcessingException(
                    "Wallet status missing");
        }

        if (response.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new PaymentProcessingException(
                    "Wallet transfer failed");
        }
    }
}