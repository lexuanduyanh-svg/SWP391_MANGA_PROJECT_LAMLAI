package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.LoginRequest;
import com.mangastudio.workflow.dtos.LoginResponse;
import java.util.Optional;

public interface AuthService {
  Optional<LoginResponse> login(LoginRequest request);
}
