package com.substring.auth.auth_app_backend.controller;

import com.substring.auth.auth_app_backend.dto.UserDto;
import com.substring.auth.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> listuser(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getuserbyemail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
    @DeleteMapping("/{userId}")
    public void deleteuserId(@PathVariable String userId)
    {
        userService.deleteUser(userId);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateuser(@PathVariable String userId,@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.updateUser(userDto,userId));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getuserbyId(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}
