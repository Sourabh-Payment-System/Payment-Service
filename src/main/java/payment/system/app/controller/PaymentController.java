package payment.system.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.TransferRequest;
import payment.system.app.entity.Transaction;
import payment.system.app.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfer")
    public Transaction transfer(@RequestBody TransferRequest request) {
        return paymentService.transferMoney(request);
    }
}