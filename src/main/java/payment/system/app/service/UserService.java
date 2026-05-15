package payment.system.app.service;

import payment.system.app.dto.CreateUserRequest;
import payment.system.app.entity.User;
import payment.system.app.entity.Wallet;
import payment.system.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(CreateUserRequest request) {

        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .createdAt(LocalDateTime.now())
                .wallet(wallet)
                .build();

        wallet.setUser(user);

        return userRepository.save(user);
    }
}