package com.substring.auth.auth_app_backend.controller;

import com.substring.auth.auth_app_backend.Repositories.RefreshTokenRepository;
import com.substring.auth.auth_app_backend.Repositories.UserRepository;
import com.substring.auth.auth_app_backend.dto.LoginRequest;
import com.substring.auth.auth_app_backend.dto.RefreshTokenRequest;
import com.substring.auth.auth_app_backend.dto.TokenResponse;
import com.substring.auth.auth_app_backend.dto.UserDto;
import com.substring.auth.auth_app_backend.entities.RefreshToken;
import com.substring.auth.auth_app_backend.entities.User;
import com.substring.auth.auth_app_backend.security.CookieService;
import com.substring.auth.auth_app_backend.security.JwtService;
import com.substring.auth.auth_app_backend.services.AuthService;
import com.substring.auth.auth_app_backend.services.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        Authentication authenticate = authenticate(loginRequest);
        User user=userRepository.findByEmail(loginRequest.email()).orElseThrow(()->new BadCredentialsException("invalid username"));
        if(!user.isEnable()){
            throw new DisabledException("user is disabled");
        }
        String jti= UUID.randomUUID().toString();
        var refreshTokenOb= RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlseconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenOb);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken= jwtService.generateRefreshToken(user,refreshTokenOb.getJti());
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlseconds());
        cookieService.addnostoreheaders(response);
        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlseconds(), modelMapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try{
          return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.passwords()));
        }
        catch (Exception e){
           throw new BadCredentialsException("Invalid username not found");
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false)
                                                      RefreshTokenRequest body,HttpServletRequest request
            ,HttpServletResponse response){
           String refreshtoken=refreshtTokenFromRequest(body,request).orElseThrow(()->new BadCredentialsException("token not found"));
           if(!jwtService.isRefreshToken(refreshtoken))
           {
               throw new BadCredentialsException("invalid refresh token type");
           }
           String jti = jwtService.getJti(refreshtoken);
           UUID userId= jwtService.getUserId(refreshtoken);
        RefreshToken token = refreshTokenRepository.findByjti(jti).orElseThrow(() -> new BadCredentialsException(
                "refresh token not found"
        ));
         if(token.isRevoked())
             throw new BadCredentialsException("refresh token is revoked");
         if(token.getExpiredAt().isBefore(Instant.now()))
             throw  new BadCredentialsException("token is expired");
         if(!token.getUser().getId().equals(userId))
             throw new BadCredentialsException("user id is not matching");
         token.setRevoked(true);
         String newjti=UUID.randomUUID().toString();
         token.setReplacedByToken(newjti);
         refreshTokenRepository.save(token);
        User user = token.getUser();
        RefreshToken build = RefreshToken.builder()
                .jti(newjti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlseconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(build);
        String newaccesstoken= jwtService.generateAccessToken(user);
        String newrefreshtoken= jwtService.generateRefreshToken(user,newjti);
        cookieService.attachRefreshCookie(response,newrefreshtoken,(int)jwtService.getRefreshTtlseconds());
        cookieService.addnostoreheaders(response);
        return ResponseEntity.ok(TokenResponse.of(newaccesstoken,newrefreshtoken, jwtService.getRefreshTtlseconds(),modelMapper.map(user,UserDto.class)));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        String refreshToken=refreshtTokenFromRequest(null,request).orElse(null);
        if(refreshToken!=null)
        {
            try {
                String jti = jwtService.getJti(refreshToken);
                refreshTokenRepository.findByjti(jti).ifPresent(
                        token -> {
                            token.setRevoked(true);
                            refreshTokenRepository.save(token);
                        }
                );
            }catch (JwtException ignored){}
        }
        cookieService.clearRefreshCookie(response);
        cookieService.addnostoreheaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
    private Optional<String> refreshtTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }

        if (body != null && body.refreshToken() != null) {
            return Optional.of(body.refreshToken());
        }

        return Optional.empty();
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
