package uz.sdg.sos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.sdg.sos.base.ApiResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(

            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)

            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String userPhone;

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            jwt = authHeader.substring(7);
            userPhone = jwtService.extractUsername(jwt);
            if (userPhone != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userPhone);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(),
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            if (e instanceof AccessDeniedException) {
                writerErrorResp(e, response, 403, objectMapper);
            } else if (e instanceof JwtException) {
                writerErrorResp(e, response, 401, objectMapper);
            }else writerErrorResp(e, response, 500, objectMapper);
        }
    }

    public void writerErrorResp(Exception exception, HttpServletResponse response, int status, ObjectMapper objectMapper) throws IOException {
        PrintWriter writer = response.getWriter();
        ApiResponse<String> customResponse = new ApiResponse<>(false, exception.getMessage());
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var content = objectMapper.writeValueAsString(customResponse);
        writer.println(content);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return (request.getRequestURI().startsWith("/sos/api/auth"));
    }
}
