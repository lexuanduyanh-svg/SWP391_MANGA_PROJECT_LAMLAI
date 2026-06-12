package com.mangaworkflow.api.application.auth;

import com.mangaworkflow.api.domain.auth.LoginRequest;
import com.mangaworkflow.api.domain.auth.LoginResponse;
import java.util.Optional;

public interface AuthService {
  Optional<LoginResponse> login(LoginRequest request);
}
