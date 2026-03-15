package com.substring.auth.auth_app_backend.Config;

public class AppConstants {
    public static final String[] Auth_Public_URLS ={
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api/v1/auth/**"
    };
    public static final String ADMIN_ROLE="ADMIN";
    public static final String GUEST_ROLE="GUEST";
}
