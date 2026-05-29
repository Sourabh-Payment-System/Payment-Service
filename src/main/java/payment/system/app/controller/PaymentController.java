package payment.system.app.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Transfer Money
     */
    @PostMapping("/transfer")
    public TransactionResponse transfer(
            @Valid
            @RequestBody
            TransferRequest request) {

        log.info(
                "Payment transfer request received: senderUserId={}, receiverUserId={}, amount={}",
                request.getSenderUserId(),
                request.getReceiverUserId(),
                request.getAmount());

        TransactionResponse response =
                paymentService.transferMoney(request);

        log.info(
                "Payment transfer completed successfully: transactionRef={}",
                response.getTransactionReference());

        return response;
    }
}