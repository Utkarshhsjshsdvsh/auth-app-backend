package com.substring.auth.auth_app_backend.security;

import com.substring.auth.auth_app_backend.Repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JwtauthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
     String header=request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try{
                if(!jwtService.isAccessToken(token)){
                    filterChain.doFilter(request,response);
                    return;
                }
                Jws<Claims> parse= jwtService.parseToken(token);
                Claims payload = parse.getPayload();
                UUID userUuid = UUID.fromString(payload.getSubject());
                userRepository.findById(userUuid).
                        ifPresent(user->{
                            if(user.isEnable()) {
                                List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream().map(role ->
                                        new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
                                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities
                                );
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                if(SecurityContextHolder.getContext().getAuthentication()==null)
                                   SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            }
                        });
            }
            catch (ExpiredJwtException e){
                request.setAttribute("error","token expired");
            }
            catch (Exception e){
                request.setAttribute("error","invalid token");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}
