package payment.system.app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.LoginRequest;
import payment.system.app.dto.LoginResponse;
import payment.system.app.entity.User;
import payment.system.app.exception.BadRequestException;
import payment.system.app.repository.UserRepository;
import payment.system.app.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;


    private final JwtUtil jwtUtil;
    
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(
            LoginRequest request) {

        User user =
                userRepository.findByUsername(
                                request.getUsername())
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid credentials"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        return LoginResponse.builder()
                .token(
                        jwtUtil.generateToken(user))
                .build();
    }
}
