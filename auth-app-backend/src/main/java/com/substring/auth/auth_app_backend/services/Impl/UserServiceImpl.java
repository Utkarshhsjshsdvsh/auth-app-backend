package com.substring.auth.auth_app_backend.services.Impl;

import com.substring.auth.auth_app_backend.Config.AppConstants;
import com.substring.auth.auth_app_backend.Repositories.RoleRepository;
import com.substring.auth.auth_app_backend.Repositories.UserRepository;
import com.substring.auth.auth_app_backend.dto.UserDto;
import com.substring.auth.auth_app_backend.entities.Provider;
import com.substring.auth.auth_app_backend.entities.Role;
import com.substring.auth.auth_app_backend.entities.User;
import com.substring.auth.auth_app_backend.exception.ResourceNotFoundException;
import com.substring.auth.auth_app_backend.helper.UserHelper;
import com.substring.auth.auth_app_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    @Override
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider()!=null ? userDto.getProvider():Provider.LOCAL);
        Role role = roleRepository.findByname("ROLE_" + AppConstants.GUEST_ROLE).orElse(null);
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        return modelMapper.map(saved, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given email")
                );
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                . findById(uId)
                .orElseThrow( () -> new ResourceNotFoundException("User not found with given id"));
        if (userDto.getName() != null) existingUser. setName(userDto.getName());
        if (userDto.getImage() != null) existingUser. setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser. setProvider(userDto.getProvider());
        if(userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        existingUser. setEnable(userDto.isEnable());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser,UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uuid = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given UUID")
                );
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() ->
                        new ResourceNotFoundException("user not found with given uuid")
                );
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public Iterable<UserDto> getAllUsers() {
        return userRepository.
                findAll().
                stream()
                .map(user->modelMapper.map(user,UserDto.class)).
                toList();
    }
}
