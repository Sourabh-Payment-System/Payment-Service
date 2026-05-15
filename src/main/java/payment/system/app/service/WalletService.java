package payment.system.app.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.AddMoneyRequest;
import payment.system.app.entity.Wallet;
import payment.system.app.exception.ResourceNotFoundException;
import payment.system.app.repository.WalletRepository;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet addMoney(AddMoneyRequest request) {

        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(
                wallet.getBalance().add(request.getAmount())
        );

        wallet.setUpdatedAt(LocalDateTime.now());

        return walletRepository.save(wallet);
    }
}