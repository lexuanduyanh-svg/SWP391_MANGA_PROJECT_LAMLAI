package com.mangaworkflow.api.service;

import com.mangaworkflow.api.model.LoginRequest;
import com.mangaworkflow.api.model.LoginResponse;
import java.util.Optional;

public interface AuthService {
  Optional<LoginResponse> login(LoginRequest request);
}
