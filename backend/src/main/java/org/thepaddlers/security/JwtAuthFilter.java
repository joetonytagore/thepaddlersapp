package org.thepaddlers.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thepaddlers.api.dto.ErrorResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthFilter extends HttpFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                DecodedJWT jwt = jwtService.verify(token);
                String sub = jwt.getSubject();
                Map<String, Object> principal = new HashMap<>();
                principal.put("id", sub);
                // copy some common claims
                var email = jwt.getClaim("email");
                if (!email.isNull()) principal.put("email", email.asString());
                Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, null);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // invalid token => send JSON error and do not proceed
                res.setStatus(401);
                res.setContentType("application/json");
                ErrorResponse er = ErrorResponse.of(org.springframework.http.HttpStatus.UNAUTHORIZED, "invalid token", req.getRequestURI(), "AUTH_INVALID_TOKEN");
                try {
                    String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(er);
                    res.getWriter().write(json);
                    res.getWriter().flush();
                } catch (Exception writeEx) {
                    // fallback to plain text
                    res.getWriter().write("invalid token");
                }
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
