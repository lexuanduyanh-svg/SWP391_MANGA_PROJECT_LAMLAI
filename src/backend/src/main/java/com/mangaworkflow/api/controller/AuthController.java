package com.mangaworkflow.api.controller;

import com.mangaworkflow.api.model.LoginRequest;
import com.mangaworkflow.api.model.LoginResponse;
import com.mangaworkflow.api.service.AuthService;
import java.util.Collections;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    Optional<LoginResponse> response = authService.login(request);
    if (response.isPresent()) {
      return ResponseEntity.ok(response.get());
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            Collections.singletonMap(
                "message",
                "Email hoặc mật khẩu không chính xác, hoặc tài khoản chưa được kích hoạt."));
  }
}
