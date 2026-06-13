package com.mangaworkflow.domain.auth;

public class LoginResponse {
  private String accessToken;
  private AuthenticatedUser user;
  private String dashboardPath;

  public LoginResponse(String accessToken, AuthenticatedUser user, String dashboardPath) {
    this.accessToken = accessToken;
    this.user = user;
    this.dashboardPath = dashboardPath;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public AuthenticatedUser getUser() {
    return user;
  }

  public String getDashboardPath() {
    return dashboardPath;
  }
}
