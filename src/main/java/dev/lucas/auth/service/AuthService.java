package dev.lucas.auth.service;

import dev.lucas.auth.dto.AuthResponse;
import dev.lucas.auth.dto.LoginRequest;
import dev.lucas.auth.dto.RegisterRequest;
import dev.lucas.entity.User;
import dev.lucas.repository.UserRepository;
import dev.lucas.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registrar(RegisterRequest request) {

        if (repository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USERNAME_JA_EXISTE");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password())).build();
        repository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()));

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
