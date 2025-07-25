package com.saas.saas_foundry_api.config;

import com.saas.saas_foundry_api.config.properties.KeycloakProperties;
import com.saas.saas_foundry_api.context.RequestContext;
import com.saas.saas_foundry_api.filter.JwtRealmFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final RequestContext requestContext;
  private final KeycloakProperties keycloakProperties;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll())
        .addFilterBefore(new JwtRealmFilter(requestContext, keycloakProperties),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}