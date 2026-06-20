package payment.system.app.facade;

import static payment.system.app.constants.ApiConstants.WALLET_TRANSFER_ENDPOINT;

import java.nio.charset.StandardCharsets;

import org.slf4j.MDC;

import static payment.system.app.constants.LogMessages.MDC_REQUEST_ID;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.config.WalletProperties;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.enums.ErrorCode;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.exception.WalletServiceException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletRetryService {

    private final RestClient restClient;
    private final WalletProperties walletProperties;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(
            name = "walletService",
            fallbackMethod = "walletCircuitBreakerFallback")
    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    WalletServiceException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2))
    public WalletTransferResponse doTransfer(
            TransferRequest request) {

        String url =
                walletProperties.getBaseUrl()
                        + WALLET_TRANSFER_ENDPOINT;

        log.info(
                "Calling wallet service. senderUserId={}, receiverUserId={}, amount={}, url={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount(),
                url);

        return restClient.post()
                .uri(url)
                .header(
                        "X-Correlation-Id",
                        MDC.get(MDC_REQUEST_ID))
                .body(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> {

                            String responseBody =
                                    new String(
                                            res.getBody().readAllBytes(),
                                            StandardCharsets.UTF_8);

                            String message = "Bad Request";

                            try {

                                JsonNode jsonNode =
                                        objectMapper.readTree(
                                                responseBody);

                                message =
                                        jsonNode.path("message")
                                                .asText("Bad Request");

                            } catch (Exception ignored) {

                                log.warn(
                                        "Unable to parse wallet error response={}",
                                        responseBody);
                            }

                            log.warn(
                                    "Wallet business validation failed. status={}, message={}",
                                    res.getStatusCode().value(),
                                    message);

                            throw new BadRequestException(
                                    message);
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> {

                            String responseBody =
                                    new String(
                                            res.getBody().readAllBytes(),
                                            StandardCharsets.UTF_8);

                            log.error(
                                    "Wallet service returned server error. status={}, response={}",
                                    res.getStatusCode().value(),
                                    responseBody);

                            throw new WalletServiceException(
                                    "Wallet service internal error",
                                    res.getStatusCode().value(),
                                    responseBody);
                        })
                .body(WalletTransferResponse.class);
    }

    @Recover
    public WalletTransferResponse recover(
            ResourceAccessException ex,
            TransferRequest request) {

        log.error(
                "Wallet service timeout after all retry attempts. senderUserId={}, receiverUserId={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex);

        throw ex;
    }

    @Recover
    public WalletTransferResponse recover(
            WalletServiceException ex,
            TransferRequest request) {

        log.error(
                "Wallet service unavailable after all retry attempts. senderUserId={}, receiverUserId={}, statusCode={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex.getStatusCode(),
                ex);

        throw ex;
    }

    @Recover
    public WalletTransferResponse recover(
            BadRequestException ex,
            TransferRequest request) {

        log.warn(
                "Wallet business validation failed. senderUserId={}, receiverUserId={}, message={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex.getMessage());

        throw ex;
    }

    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            BadRequestException ex) {

        log.warn(
                "Wallet business validation error. senderUserId={}, receiverUserId={}, message={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex.getMessage());

        throw ex;
    }

    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            WalletServiceException ex) {

        log.error(
                "Circuit breaker fallback triggered. senderUserId={}, receiverUserId={}, statusCode={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex.getStatusCode(),
                ex);

        throw new PaymentProcessingException(
                ErrorCode.WALLET_SERVICE_ERROR,
                "Wallet service is currently unavailable");
    }

    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            ResourceAccessException ex) {

        log.error(
                "Wallet timeout fallback triggered. senderUserId={}, receiverUserId={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex);

        throw new PaymentProcessingException(
                ErrorCode.WALLET_SERVICE_ERROR,
                "Wallet service request timed out");
    }

    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            CallNotPermittedException ex) {

        log.error(
                "Circuit breaker OPEN. senderUserId={}, receiverUserId={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex);

        throw new PaymentProcessingException(
                ErrorCode.WALLET_SERVICE_ERROR,
                "Wallet service temporarily unavailable");
    }

    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            Exception ex) {

        log.error(
                "Unexpected wallet service failure. senderUserId={}, receiverUserId={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                ex);

        throw new PaymentProcessingException(
                ErrorCode.WALLET_SERVICE_ERROR,
                "Wallet service temporarily unavailable");
    }
}