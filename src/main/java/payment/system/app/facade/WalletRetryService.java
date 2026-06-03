package payment.system.app.facade;

import static payment.system.app.constants.ApiConstants.WALLET_TRANSFER_ENDPOINT;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.config.WalletProperties;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.exception.WalletServiceException;
import org.springframework.retry.annotation.Backoff;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletRetryService {

    private final RestClient restClient;
    private final WalletProperties walletProperties;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(
            name = "walletService",
            fallbackMethod = "walletCircuitBreakerFallback"
    )
    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    WalletServiceException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public WalletTransferResponse doTransfer(
            TransferRequest request) {

        String url =
                walletProperties.getBaseUrl()
                        + WALLET_TRANSFER_ENDPOINT;

        log.info(
                "Calling Wallet Service. senderUserId={}, receiverUserId={}, amount={}, url={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount(),
                url);

        return restClient.post()
                .uri(url)
                .body(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> {

                            String responseBody =
                                    new String(res.getBody().readAllBytes());

                            try {


                                JsonNode jsonNode =
                                        objectMapper.readTree(responseBody);

                                String errorMessage =
                                        jsonNode.path("message")
                                                .asText("Bad Request");

                                throw new BadRequestException(
                                        errorMessage);

                            } catch (IOException e) {

                                throw new BadRequestException(
                                        "Bad Request");
                            }
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (req, res) -> {

                            String responseBody =
                                    new String(
                                            res.getBody()
                                                    .readAllBytes());

                            throw new WalletServiceException(
                                    "Wallet Internal Error",
                                    res.getStatusCode().value(),
                                    responseBody);
                        })
                .body(WalletTransferResponse.class);
    }

    @Recover
    public WalletTransferResponse recover(
            ResourceAccessException ex,
            TransferRequest request) {

        throw new PaymentProcessingException(
                "Wallet service timeout after retry attempts");
    }

    @Recover
    public WalletTransferResponse recover(
            WalletServiceException ex,
            TransferRequest request) {

        throw new PaymentProcessingException(
                "Wallet service unavailable after retry attempts");
    }
    @Recover
    public WalletTransferResponse recover(
            BadRequestException ex,
            TransferRequest request) {

        throw ex;
    }
    public WalletTransferResponse walletCircuitBreakerFallback(
            TransferRequest request,
            Exception ex) {

        log.error(
                "Wallet circuit breaker activated. reason={}",
                ex.getMessage());

        throw new PaymentProcessingException(
                "Wallet service temporarily unavailable");
    }
}
