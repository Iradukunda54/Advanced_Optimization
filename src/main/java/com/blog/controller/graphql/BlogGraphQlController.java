package com.blog.controller.graphql;

import com.blog.dto.CommentDTO;
import com.blog.dto.JwtResponse;
import com.blog.dto.PostDTO;
import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.service.PostService;
import com.blog.service.UserService;
import com.blog.service.CommentService;
import com.blog.service.TokenBlacklistService;
import com.blog.util.JwtUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BlogGraphQlController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public BlogGraphQlController(UserService userService, PostService postService, CommentService commentService,
                                 AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                                 TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @MutationMapping
    public JwtResponse login(@Argument String username, @Argument String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    @MutationMapping
    public String register(@Argument String username, @Argument String email, @Argument String password, @Argument String role) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRole(role);
        userService.createUser(dto);
        return "User registered successfully!";
    }

    @MutationMapping
    public String logout() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String headerAuth = attributes.getRequest().getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                String token = headerAuth.substring(7);
                tokenBlacklistService.blacklistToken(token);
                return "Logged out and token blacklisted";
            }
        }
        return "No token found to blacklist";
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> users() {

        return userService.getAllUsers();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR') or hasRole('READER')")
    public UserDTO user(@Argument Long id) {

        return userService.getUserById(id);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR') or hasRole('READER')")
    public List<PostDTO> posts() {
        // Return first page of posts for simplicity in GraphQL query mapping without pageable
        return postService.getAllPosts(org.springframework.data.domain.PageRequest.of(0, 50)).getContent();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR') or hasRole('READER')")
    public PostDTO post(@Argument Long id) {
        return postService.getPostById(id);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR') or hasRole('READER')")
    public List<PostDTO> searchPosts(@Argument String keyword) {
        return postService.searchPosts(keyword);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO createUser(@Argument String username, @Argument String email, @Argument String role) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setPassword("default123"); // Default password for simplicity
        return userService.createUser(dto);
    }

    @MutationMapping
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public PostDTO createPost(@Argument String title, @Argument String content, @Argument Long authorId) {
        PostDTO dto = new PostDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setAuthorId(authorId);
        return postService.createPost(dto);
    }

    @MutationMapping
    @PreAuthorize("hasRole('READER') or hasRole('AUTHOR') or hasRole('ADMIN')")
    public CommentDTO addComment(@Argument String content, @Argument Long postId, @Argument Long authorId) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(content);
        dto.setPostId(postId);
        dto.setAuthorId(authorId);
        return commentService.addComment(dto);
    }
}
