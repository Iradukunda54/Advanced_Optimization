package com.blog.controller.rest;

import com.blog.dto.ApiResponse;
import com.blog.dto.JwtResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import com.blog.repository.RoleRepository;
import com.blog.model.Role;
import com.blog.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, 
                          RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ApiResponse<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ApiResponse.success("Login successful", new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/register")
    public ApiResponse<String> registerUser(@Valid @RequestBody UserDTO signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ApiResponse.error("Error: Username is already taken!");
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ApiResponse.error("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());
        
        String roleName = signUpRequest.getRole() == null ? "READER" : signUpRequest.getRole().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
        user.getRoles().add(role);
        user.setRole(roleName);

        userRepository.save(user);

        return ApiResponse.success("User registered successfully!", null);
    }

    @GetMapping("/oauth2/success")
    public ApiResponse<String> oauth2Success(@RequestParam String token) {
        return ApiResponse.success("Google Login successful! Copy your token.", token);
    }
}
