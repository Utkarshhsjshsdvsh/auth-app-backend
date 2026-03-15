package com.substring.auth.auth_app_backend.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.substring.auth.auth_app_backend.dto.ApiError;
import com.substring.auth.auth_app_backend.security.JwtauthenticationFilter;
import com.substring.auth.auth_app_backend.security.oauth2successhandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtauthenticationFilter jwtauthenticationFilter;
    @Autowired
    private oauth2successhandler oauth2successhandle;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AppConstants.Auth_Public_URLS).permitAll()
                        .requestMatchers("api/v1/users/**").hasRole(AppConstants.ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET).hasRole(AppConstants.GUEST_ROLE)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2->oauth2.successHandler(oauth2successhandle)
                        .failureHandler(null))
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            authException.printStackTrace();
            response.setStatus(401);
            response.setContentType("application/json");
            String message =authException.getMessage();
            String error =(String) request.getAttribute("error");
            if(error!=null)
            {
                message=error;
            }

//            Map<String, String> errorMap = Map.of(
//                    "message", message,
//                    "statusCode", "401"
//            );
              ApiError apierror = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorised access", message, request.getRequestURI(), true);
              var objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(apierror));
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            String message =accessDeniedException.getMessage();
            String error =(String) request.getAttribute("error");
            if(error!=null)
            {
                message=error;
            }
            ApiError apierror = ApiError.of(HttpStatus.FORBIDDEN.value(), "forbidden access", message, request.getRequestURI(), true);
            var objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(apierror));
        })
                )
        .addFilterBefore(jwtauthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

