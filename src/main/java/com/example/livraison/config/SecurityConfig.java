package com.example.livraison.config;

import com.example.livraison.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Chaine de securite pour l'API REST (stateless, sans redirection de login).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "USER")
                        .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/livreurs/**", "/api/deliverers/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/orders/*/assign", "/api/orders/*/pickup", "/api/orders/*/start",
                                "/api/orders/*/deliver", "/api/orders/*/cancel", "/api/orders/*/incidents").hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .anyRequest().hasAnyRole("ADMIN", "MANAGER"))
                .httpBasic(basic -> {});
        return http.build();
    }

    /**
     * Chaine de securite pour l'interface web Thymeleaf (formulaire de login classique).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                        .requestMatchers("/livreurs/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/statistiques/**").hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}
