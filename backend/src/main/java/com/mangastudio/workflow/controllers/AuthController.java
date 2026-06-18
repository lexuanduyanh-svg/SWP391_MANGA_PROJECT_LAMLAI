package com.mangastudio.workflow.controllers;

import com.mangastudio.workflow.dtos.LoginRequest;
import com.mangastudio.workflow.dtos.LoginResponse;
import com.mangastudio.workflow.services.AuthService;
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
                "Email hoÃ¡ÂºÂ·c mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u khÃƒÂ´ng chÃƒÂ­nh xÃƒÂ¡c, hoÃ¡ÂºÂ·c tÃƒÂ i khoÃ¡ÂºÂ£n chÃ†Â°a Ã„â€˜Ã†Â°Ã¡Â»Â£c kÃƒÂ­ch hoÃ¡ÂºÂ¡t."));
  }
}
