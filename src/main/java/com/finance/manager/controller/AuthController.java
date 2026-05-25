package com.finance.manager.controller;

import com.finance.manager.dto.UserDto;
import com.finance.manager.entity.User;
import com.finance.manager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto.AuthResponse> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        User user = userService.register(request);
        return new ResponseEntity<>(
                new UserDto.AuthResponse("User registered successfully", user.getId()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.MessageResponse> login(@Valid @RequestBody UserDto.LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            
            return new ResponseEntity<>(
                    new UserDto.MessageResponse("Login successful"),
                    HttpStatus.OK
            );
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    new UserDto.MessageResponse("Invalid credentials"),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<UserDto.MessageResponse> logout(HttpServletRequest httpRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ResponseEntity<>(
                new UserDto.MessageResponse("Logout successful"),
                HttpStatus.OK
        );
    }
}
