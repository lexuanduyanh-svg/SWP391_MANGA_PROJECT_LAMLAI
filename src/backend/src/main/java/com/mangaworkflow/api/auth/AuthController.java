package com.mangaworkflow.api.auth;

import com.mangaworkflow.domain.auth.LoginRequest;
import com.mangaworkflow.domain.auth.LoginResponse;
import com.mangaworkflow.application.auth.AuthService;
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
                "Email hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c, hoáº·c tÃ i khoáº£n chÆ°a Ä‘Æ°á»£c kÃ­ch hoáº¡t."));
  }
}
