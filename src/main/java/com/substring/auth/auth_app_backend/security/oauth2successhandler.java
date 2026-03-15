package com.substring.auth.auth_app_backend.security;

import com.substring.auth.auth_app_backend.Repositories.UserRepository;
import com.substring.auth.auth_app_backend.entities.Provider;
import com.substring.auth.auth_app_backend.entities.RefreshToken;
import com.substring.auth.auth_app_backend.entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@AllArgsConstructor
public class oauth2successhandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oath2user =(OAuth2User)authentication.getPrincipal();
        String registrationId="Unknown";
        if(authentication instanceof OAuth2AuthenticationToken token)
        {
              registrationId= token.getAuthorizedClientRegistrationId();
        }
        User user;
        switch (registrationId){
            case "google"-> {
                String googleId = oath2user.getAttributes().getOrDefault("sub", "").toString();
                String emailId = oath2user.getAttributes().getOrDefault("email", "").toString();
                String name = oath2user.getAttributes().getOrDefault("name", "").toString();
                user = User.builder()
                        .email(emailId)
                        .name(name)
                        .provider(Provider.GOOGLE)
                        .enable(true)
                        .build();
                userRepository.findByEmail(emailId).ifPresentOrElse(user1 -> {
                        },
                        () -> {
                            userRepository.save(user);
                        });
            }
            default -> {
                throw new RuntimeException("invalid registration id");
            }
        }
        String jti = UUID.randomUUID().toString();
        RefreshToken build = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlseconds()))
                .build();
        String refreshtoken= jwtService.generateRefreshToken(user,jti);
        cookieService.attachRefreshCookie(response,refreshtoken,(int)jwtService.getRefreshTtlseconds());
        response.getWriter().write("login successful");
    }
}
