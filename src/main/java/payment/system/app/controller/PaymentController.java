package payment.system.app.controller;

import static payment.system.app.constants.ApiConstants.API_V1_PAYMENTS;
import static payment.system.app.constants.ApiConstants.PAYMENT_TRANSFER_SUCCESS_MESSAGE;
import static payment.system.app.constants.ApiConstants.TRANSFER_ENDPOINT;
import static payment.system.app.constants.LogMessages.*;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.ApiResponse;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransferRequest;
import payment.system.app.service.PaymentService;

@RestController
@RequestMapping(API_V1_PAYMENTS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(TRANSFER_ENDPOINT)
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER)
            @NotBlank(message = "Idempotency-Key header is mandatory")
            String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
    	try {

        MDC.put(
        		MDC_SENDER_USER_ID,
                String.valueOf(
                        request.getSenderUserId()));

        

        log.info(
        	    PAYMENT_TRANSFER_INITIATED,
        	    request.getSenderUserId(),
        	    request.getReceiverUserId(),
        	    request.getAmount());

            TransactionResponse response =
                    paymentService.transferMoney(
                            request,idempotencyKey);


            log.info(
                    "Payment transfer completed successfully. status={}",
                    response.getStatus());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            PAYMENT_TRANSFER_SUCCESS_MESSAGE,
                            response));}
finally {
		   MDC.remove(MDC_SENDER_USER_ID);
		   MDC.remove(MDC_TRANSACTION_REF);
}

        
    }
}