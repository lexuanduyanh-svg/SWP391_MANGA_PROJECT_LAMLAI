package com.mangaworkflow.application.auth;

import com.mangaworkflow.domain.auth.LoginRequest;
import com.mangaworkflow.domain.auth.LoginResponse;
import java.util.Optional;

public interface AuthService {
  Optional<LoginResponse> login(LoginRequest request);
}
