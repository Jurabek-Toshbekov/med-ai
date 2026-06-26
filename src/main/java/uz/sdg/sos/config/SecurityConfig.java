package uz.sdg.sos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uz.sdg.sos.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {"uz.sdg.sos"})
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()

                // === PUBLIC ===
                .antMatchers(PUBLIC_URLS).permitAll()
                .antMatchers(SWAGGER_URLS).permitAll()

                // === ADMIN ONLY ===
                .antMatchers(ADMIN_ONLY_URLS).hasAuthority("ADMIN")

                // === AUTHENTICATED (any role) ===
                .anyRequest().authenticated()

                .and()
                .exceptionHandling()
                .accessDeniedHandler((req, res, ex) ->
                        jwtAuthFilter.writerErrorResp(ex, res, 403, objectMapper))

                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Auth yo'q — ochiq
    private static final String[] PUBLIC_URLS = {
            "/sdg/uz/login",
            "/api/v1/clinic-applications/apply",
    };

    // Faqat ADMIN
    private static final String[] ADMIN_ONLY_URLS = {
            "/api/v1/users/**",
            "/api/v1/clinics/**",
            "/api/v1/clinic-applications/**",
            "/api/v1/admin/**",
            "/api/v1/lab/admin/**",
    };

    // Swagger (dev uchun)
    private static final String[] SWAGGER_URLS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
    };
}
