package com.poly.apigateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtGlobalFilter extends OncePerRequestFilter implements Ordered {

  private final GatewayJwtService jwtService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    if (isPublicRoute(path, method)) {
      filterChain.doFilter(new HeaderMapRequestWrapper(request), response);
      return;
    }

    String token = null;
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token == null) {
      onError(response, "Missing or invalid access_token cookie");
      return;
    }
    if (!jwtService.validateToken(token)) {
      onError(response, "Invalid or expired token");
      return;
    }

    String userId = jwtService.extractUserId(token);
    List<String> roles = jwtService.extractRoles(token);
    String rolesString = String.join(",", roles);

    HeaderMapRequestWrapper wrapper = new HeaderMapRequestWrapper(request);
    wrapper.addHeader("X-User-Id", userId);
    wrapper.addHeader("X-User-Roles", rolesString);

    filterChain.doFilter(wrapper, response);
  }

  private boolean isPublicRoute(String path, String method) {
    if (pathMatcher.match("/api/auth/**", path)) {
      return true;
    }
    if (pathMatcher.match("/api/hotels/**", path) && "GET".equalsIgnoreCase(method)) {
      return true;
    }
    if (pathMatcher.match("/api/payments/sepay-webhook", path) && "POST".equalsIgnoreCase(method)) {
      return true;
    }
    return false;
  }

  private void onError(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", HttpStatus.UNAUTHORIZED.value());
    body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
    body.put("message", message);

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(body);
      response.getOutputStream().write(bytes);
    } catch (JsonProcessingException e) {
      // ignore
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> headerMap = new LinkedHashMap<>();

    public HeaderMapRequestWrapper(HttpServletRequest request) {
      super(request);
      // Strip incoming spoofed headers
      Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames != null) {
        while (headerNames.hasMoreElements()) {
          String name = headerNames.nextElement();
          if (!"X-User-Id".equalsIgnoreCase(name) && !"X-User-Roles".equalsIgnoreCase(name)) {
            headerMap.put(name.toLowerCase(), request.getHeader(name));
          }
        }
      }
    }

    public void addHeader(String name, String value) {
      headerMap.put(name.toLowerCase(), value);
    }

    @Override
    public String getHeader(String name) {
      return headerMap.get(name.toLowerCase());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      return Collections.enumeration(headerMap.keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      String value = headerMap.get(name.toLowerCase());
      if (value != null) {
        return Collections.enumeration(Collections.singletonList(value));
      }
      return Collections.emptyEnumeration();
    }
  }
}
