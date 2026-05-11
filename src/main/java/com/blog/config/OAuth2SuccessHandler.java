package com.blog.config;

import com.blog.model.User;
import com.blog.model.Role;
import com.blog.repository.UserRepository;
import com.blog.repository.RoleRepository;
import com.blog.util.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OAuth2SuccessHandler(JwtUtils jwtUtils, UserRepository userRepository, RoleRepository roleRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); // Google unique ID

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email); // Use email as username for OAuth2 users
            user.setPassword(""); // No password for OAuth2 users
            Role role = roleRepository.findByName("READER")
                    .orElseGet(() -> roleRepository.save(new Role("READER")));
            user.getRoles().add(role);
            user.setRole("READER");
            user.setProvider("GOOGLE");
            user.setProviderId(providerId);
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());
        String targetUrl = UriComponentsBuilder.fromUriString("/api/auth/oauth2/success")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
