package uz.sdg.sos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()

                .antMatchers(OPEN_ALL_URLS).permitAll()
                .antMatchers("/", "/index").permitAll()
//                .antMatchers(HttpMethod.POST,OPEN_ALL_URLS).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    jwtAuthFilter.writerErrorResp(accessDeniedException,response, 403,objectMapper);
                })
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        System.out.println("dcsacsacsacsacsa");
        return http.build();

    }

    // barcha ochiq yo'llar Real server uchun
    private static final String[] OPENED_URLS = {
            "/sos/api/auth/one",
            "/sos/api/auth/two",
            "/sos/api/auth/three",
            "/sos/api/auth/login",
            "/sos/api/auth/forget_password/**",
            "/guest"
    };

    // swagger uchun ishlatiladigan ichiq yollar
    private static final String[] SWAGGER_WHITELIST = {
            "/sos/api/auth/login",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui.html#/"
    };

    // test uchun ishlatiladigan ichiq yolalr
    private static final String[] OPEN_ALL_URLS = {
            "/**",

    };
}