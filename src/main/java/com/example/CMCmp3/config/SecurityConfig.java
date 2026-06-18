package com.example.CMCmp3.config;

import com.example.CMCmp3.security.JwtAuthenticationFilter;
import com.example.CMCmp3.security.OAuth2AuthenticationSuccessHandler;
import com.example.CMCmp3.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Auth + OAuth2
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/auth/verify-login-otp",
                                "/login/oauth2/**",
                                "/oauth2/redirect/**"
                        ).permitAll()

                        // ZingChart realtime
                        .requestMatchers("/api/charts/realtime").permitAll()

                        // Stream nhạc public
                        .requestMatchers(HttpMethod.GET, "/api/songs/stream/**").permitAll()

                        // Public GET songs/playlists/artists
                        .requestMatchers(HttpMethod.GET, "/api/songs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/playlists/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/artists/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags/**").permitAll()

                        // ✅ ALBUMS:
                        // (1) /me phải đăng nhập (đặt TRƯỚC để không bị permitAll phía dưới nuốt mất)
                        .requestMatchers(HttpMethod.GET, "/api/albums/me/**").authenticated()

                        // (2) Public albums (trang chủ, chi tiết, top, share, songs...)
                        .requestMatchers(HttpMethod.GET, "/api/albums/**").permitAll()

                        // Search, ảnh tĩnh, preflight
                        .requestMatchers("/api/search").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Artist Verification
                        .requestMatchers("/api/admin/artist-verification-requests/**").hasRole("ADMIN")
                        .requestMatchers("/api/me/artist-verification-requests/**").hasAnyRole("USER", "ARTIST", "ADMIN")

                        // Upload file
                        .requestMatchers(HttpMethod.POST, "/api/files/upload").authenticated()

                        // Còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o.successHandler(oAuth2AuthenticationSuccessHandler))
                .userDetailsService(userDetailsServiceImpl)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 1. Tạo danh sách các domain mặc định (Hardcode để test local và sơ cua)
        List<String> origins = new java.util.ArrayList<>(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:8082",
                "http://127.0.0.1:8082",
                "https://cmcmp3-production.up.railway.app"
        ));

        // 2. Nếu có biến môi trường, gộp thêm vào danh sách trên
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            origins.addAll(Arrays.asList(allowedOrigins.split(",")));
        }

        // 3. Set vào cấu hình
        cfg.setAllowedOrigins(origins);

        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
