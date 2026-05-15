package payment.system.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.AddMoneyRequest;
import payment.system.app.entity.Wallet;
import payment.system.app.service.WalletService;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/add-money")
    public Wallet addMoney(@RequestBody AddMoneyRequest request) {
        return walletService.addMoney(request);
    }
}