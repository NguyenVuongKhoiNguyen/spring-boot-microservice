package com.poly.user.controllers;

import com.poly.user.configs.CookieProperties;
import com.poly.user.dtos.requests.LoginRequest;
import com.poly.user.dtos.requests.RefreshRequest;
import com.poly.user.dtos.requests.RegisterRequest;
import com.poly.user.dtos.responses.AuthResponse;
import com.poly.user.dtos.responses.LoginResult;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final CookieProperties cookieProperties;
  private final long accessExpiration;
  private final long refreshExpiration;

  public AuthController(
      AuthService authService,
      CookieProperties cookieProperties,
      @Value("${jwt.access-expiration}") long accessExpiration,
      @Value("${jwt.refresh-expiration}") long refreshExpiration) {
    this.authService = authService;
    this.cookieProperties = cookieProperties;
    this.accessExpiration = accessExpiration;
    this.refreshExpiration = refreshExpiration;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResult result = authService.login(request);

    ResponseCookie accessCookie =
        ResponseCookie.from(cookieProperties.getAccessName(), result.accessToken())
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path("/")
            .maxAge(accessExpiration / 1000)
            .sameSite(cookieProperties.getSameSite())
            .build();

    ResponseCookie refreshCookie =
        ResponseCookie.from(cookieProperties.getRefreshName(), result.refreshToken())
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path("/api/auth")
            .maxAge(refreshExpiration / 1000)
            .sameSite(cookieProperties.getSameSite())
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(result.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookieProperties.getRefreshName().equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    authService.logout(refreshToken);

    ResponseCookie accessCookie =
        ResponseCookie.from(cookieProperties.getAccessName(), "")
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path("/")
            .maxAge(0)
            .sameSite(cookieProperties.getSameSite())
            .build();

    ResponseCookie refreshCookie =
        ResponseCookie.from(cookieProperties.getRefreshName(), "")
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path("/api/auth")
            .maxAge(0)
            .sameSite(cookieProperties.getSameSite())
            .build();

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .build();
  }
}
