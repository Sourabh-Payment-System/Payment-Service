package payment.system.app.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatusCode;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;

import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletFacadeService {

    private final RestClient restClient;

    @Value("${wallet.service.base-url}")
    private String walletServiceBaseUrl;

    /**
     * Transfer Money
     */
    public WalletTransferResponse transferMoney(
            TransferRequest request) {

        log.info(
                "Calling wallet service for money transfer: senderUserId={}, receiverUserId={}, amount={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount());

        validateTransferRequest(request);

        String url =
                walletServiceBaseUrl
                        + "/wallets/transfer";

        try {

            WalletTransferResponse response =
                    restClient.post()
                            .uri(url)
                            .body(request)
                            .retrieve()
                            .onStatus(
                                    HttpStatusCode::is4xxClientError,
                                    (req, res) -> {

                                        log.error(
                                                "Wallet service returned client error: status={}",
                                                res.getStatusCode());

                                        throw new BadRequestException(
                                                "Invalid wallet transfer request");
                                    })
                            .onStatus(
                                    HttpStatusCode::is5xxServerError,
                                    (req, res) -> {

                                        log.error(
                                                "Wallet service returned server error: status={}",
                                                res.getStatusCode());

                                        throw new PaymentProcessingException(
                                                "Wallet service is currently unavailable");
                                    })
                            .body(
                                    WalletTransferResponse.class);

            validateWalletTransferResponse(
                    response);

            log.info(
                    "Wallet service transfer completed successfully: walletTransactionReference={}",
                    response.getWalletTransactionReference());

            return response;

        } catch (BadRequestException ex) {

            log.error(
                    "Bad request while calling wallet service",
                    ex);

            throw ex;

        } catch (PaymentProcessingException ex) {

            log.error(
                    "Payment processing error while calling wallet service",
                    ex);

            throw ex;

        } catch (RestClientResponseException ex) {

            log.error(
                    "Wallet service REST error: statusCode={}, responseBody={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);

            throw new PaymentProcessingException(
                    "Wallet service communication failed");

        } catch (Exception ex) {

            log.error(
                    "Unexpected error occurred while calling wallet service",
                    ex);

            throw new PaymentProcessingException(
                    "Unexpected error while processing wallet transfer");
        }
    }

    /**
     * Validate Transfer Request
     */
    private void validateTransferRequest(
            TransferRequest request) {

        log.info(
                "Validating wallet transfer request");

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

        log.info(
                "Wallet transfer request validated successfully");
    }

    /**
     * Validate Wallet Transfer Response
     */
    private void validateWalletTransferResponse(
            WalletTransferResponse response) {

        log.info(
                "Validating wallet transfer response");

        if (response == null) {

            log.error(
                    "Wallet transfer response is null");

            throw new PaymentProcessingException(
                    "Wallet service returned null response");
        }

        if (response.getWalletTransactionReference() == null
                || response.getWalletTransactionReference()
                        .isBlank()) {

            log.error(
                    "Wallet transaction reference is missing");

            throw new PaymentProcessingException(
                    "Wallet transaction reference is missing");
        }

        if (response.getStatus() == null
                || response.getStatus()
                        .isBlank()) {

            log.error(
                    "Wallet transaction status is missing");

            throw new PaymentProcessingException(
                    "Wallet transaction status is missing");
        }

        log.info(
                "Wallet transfer response validated successfully");
    }
}