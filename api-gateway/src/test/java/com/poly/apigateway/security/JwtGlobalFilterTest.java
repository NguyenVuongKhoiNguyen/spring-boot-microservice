package com.poly.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtGlobalFilterTest {

  @Mock private GatewayJwtService jwtService;
  @Mock private FilterChain filterChain;
  private JwtGlobalFilter jwtGlobalFilter;

  @BeforeEach
  void setUp() {
    jwtGlobalFilter = new JwtGlobalFilter(jwtService);
  }

  @Test
  void filter_publicRoute_allowsRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtGlobalFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(any(), eq(response));
    verifyNoInteractions(jwtService);
  }

  @Test
  void filter_noAuthHeader_returns401() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtGlobalFilter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    verifyNoInteractions(filterChain);
  }

  @Test
  void filter_validToken_stripsAndAddsHeaders() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
    request.setCookies(new jakarta.servlet.http.Cookie("access_token", "valid.token"));
    request.addHeader("X-User-Id", "spoof");
    request.addHeader("X-User-Roles", "ADMIN");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateToken("valid.token")).thenReturn(true);
    when(jwtService.extractUserId("valid.token")).thenReturn("1");
    when(jwtService.extractRoles("valid.token")).thenReturn(List.of("USER"));

    jwtGlobalFilter.doFilterInternal(request, response, filterChain);

    ArgumentCaptor<HttpServletRequest> reqCaptor =
        ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(filterChain).doFilter(reqCaptor.capture(), eq(response));

    HttpServletRequest mutatedReq = reqCaptor.getValue();
    assertThat(mutatedReq.getHeader("X-User-Id")).isEqualTo("1");
    assertThat(mutatedReq.getHeader("X-User-Roles")).isEqualTo("USER");
  }
}
