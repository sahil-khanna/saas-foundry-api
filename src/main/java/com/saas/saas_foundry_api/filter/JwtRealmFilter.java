package com.saas.saas_foundry_api.filter;

import com.saas.saas_foundry_api.config.properties.KeycloakProperties;
import com.saas.saas_foundry_api.context.RequestContext;
import com.saas.saas_foundry_api.enums.TenantType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class JwtRealmFilter extends OncePerRequestFilter {

  private RequestContext requestContext;
  private KeycloakProperties keycloakProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtRealmFilter(RequestContext requestContext, KeycloakProperties keycloakProperties) {
    this.requestContext = requestContext;
    this.keycloakProperties = keycloakProperties;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractBearerToken(request);
    if (token == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
      return;
    }

    try {
      String issuer = extractIssuer(token);
      JwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
      decoder.decode(token);
      String realm = issuer.substring(issuer.lastIndexOf("/") + 1);
      requestContext.setRealm(realm);

      if ("master".equals(realm)) {
        requestContext.setType(TenantType.SUPER_ADMIN);
      }
      else if (keycloakProperties.getOrganizationRealm().equals(realm)) {
        requestContext.setType(TenantType.ORGANIZATION);
      }
      else {
        requestContext.setType(TenantType.CLIENT);
      }
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String extractBearerToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private String extractIssuer(String token) throws IOException {
    String[] parts = token.split("\\.");
    if (parts.length < 2)
      throw new IllegalArgumentException("Invalid JWT format");

    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    Map<?, ?> payloadMap = objectMapper.readValue(payload, Map.class);

    Object iss = payloadMap.get("iss");
    if (iss == null)
      throw new IllegalArgumentException("Missing 'iss' in JWT");

    return iss.toString();
  }
}
