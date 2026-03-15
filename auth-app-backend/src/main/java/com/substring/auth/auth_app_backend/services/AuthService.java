package com.substring.auth.auth_app_backend.services;

import com.substring.auth.auth_app_backend.dto.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
