package com.example.skillswap.filter;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.AccountStatusService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AccountStatusService accountStatusService;

    public JwtFilter(
            JwtUtils jwtUtil,
            UserDetailsService userDetailsService,
            AccountStatusService accountStatusService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.accountStatusService = accountStatusService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateJwtToken(token)) {
                String username = jwtUtil.getUserFromToken(token);

                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails instanceof User user
                            && accountStatusService.isBlocked(user)) {
                        SecurityContextHolder.clearContext();
                        writeUnauthorized(response, "This account is suspended or banned.");
                        return;
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (UsernameNotFoundException ex) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(response, "This account is no longer available.");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
