package com.example.backend.security;

import com.example.backend.auth.service.JWTservice;
import com.example.backend.auth.service.UsersServices;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTservice jwtservice;
    private final UsersServices userService;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint; // inject your RestAuthenticationEntryPoint bean

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtservice.extractUsername(token); // can throw ExpiredJwtException or other JwtException
            }

            if (username != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtservice.validateToken(token, userDetails)) {
                    var claims = jwtservice.extractRoles(token);
                    var authorities = claims.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList();

                    var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // Token problems (expired, malformed, unsupported, etc)
            // Wrap into an AuthenticationException and let AuthenticationEntryPoint handle it
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Invalid or expired JWT: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
            // do NOT continue the filter chain
        } catch (Exception e) {
            // Any other failure while processing token -> treat as authentication failure
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Authentication failed: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
        }
    }
}
