package payment.system.app.controller;

import static payment.system.app.constants.ApiConstants.API_V1_PAYMENTS;
import static payment.system.app.constants.ApiConstants.PAYMENT_TRANSFER_SUCCESS_MESSAGE;
import static payment.system.app.constants.ApiConstants.TRANSFER_ENDPOINT;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import payment.system.app.dto.ApiResponse;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransferRequest;
import payment.system.app.service.PaymentService;

@RestController
@RequestMapping(API_V1_PAYMENTS)
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(TRANSFER_ENDPOINT)
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request) {

    	log.info(
    	        "Payment transfer request received: senderUserId={}, receiverUserId={}, amount={}",
    	        request.getSenderUserId(),
    	        request.getReceiverUserId(),
    	        request.getAmount());
    	
    	TransactionResponse response =
                paymentService.transferMoney(request);
    	
    	log.info(
    	        "Payment transfer completed successfully. transactionRef={}",
    	        response.getTransactionReference());

        return ResponseEntity.ok(
                ApiResponse.success(
                        PAYMENT_TRANSFER_SUCCESS_MESSAGE,
                        response));
    }
}