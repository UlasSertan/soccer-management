package com.turkcell.soccer.security.common;

import com.turkcell.soccer.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.NonNull;


import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,    // Incoming HTTP request
            @NonNull HttpServletResponse response,  // Outgoing HTTP response
            @NonNull FilterChain chain)             // Let request to continue to the next filter
            throws IOException, ServletException {

            String authHeader = request.getHeader("Authorization");
            final String token;
            String username;

            // Allow non auth requests pass by
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }



            token = authHeader.substring(7);
            username = jwtUtil.extractUsername(token);


            // Check if previous filters have already authenticated the user

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Check if the token is valid
                if (jwtUtil.validateToken(token)) {
                    // Authenticate the user
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // Add metadata
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Assign the auth token to security context for later filters
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            chain.doFilter(request, response);
    }
}
