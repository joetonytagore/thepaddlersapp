package org.thepaddlers.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;

public class DevAuthFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            // dev token format: dev-token-for-user-<id>
            if (token.startsWith("dev-token-for-user-")) {
                String idStr = token.substring("dev-token-for-user-".length());
                // set Authentication with principal as a map containing id
                try {
                    long id = Long.parseLong(idStr);
                    Map<String,Object> principal = Map.of("id", id);
                    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {}
            }
        }
        chain.doFilter(req, res);
    }
}

