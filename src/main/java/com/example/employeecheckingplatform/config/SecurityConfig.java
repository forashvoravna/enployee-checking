package com.example.employeecheckingplatform.config;

import com.example.employeecheckingplatform.config.jwt.JwtAuthFilter;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final FoydalanuvchiRepository userRepo;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User topilmadi"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // ⚡️ Deprecated ogohlantirishsiz to‘g‘ri yo‘l:
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS ON
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight'ni qo'yib yuborish
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/api/urinish/test","/hikvision/events").permitAll()
                        .requestMatchers("/api/foydalanuvchi/**", "/api/tashkilot/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        // RUXSAT BERILGAN FRONTEND ORIGINLAR
        cfg.setAllowedOrigins(List.of("https://facequiz.mv","https://api.facequiz.mv","https://test.facequiz.mv"));
        // HTTP metodlar
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        // HTTP headerlar (JWT uchun 'Authorization' shart)
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        // Agar cookie/session yubormasangiz, shart emas. JWT Bearer bo‘lsa odatda false.
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);
        // Brauzer o‘qishi kerak bo‘lgan response headerlar (ixtiyoriy)
        cfg.setExposedHeaders(List.of("Location"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }


}
