package com.orders.api.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    String[] allAuthorities = Arrays.stream(Role.values()).map(Enum::name).toArray(String[]::new);
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/check/authorities").hasAnyAuthority(allAuthorities)
                        .requestMatchers(HttpMethod.GET,"/users").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET, "/orders/variants").permitAll()
                        .requestMatchers(HttpMethod.POST, "/order/create").hasAnyAuthority(allAuthorities)
                        .requestMatchers(HttpMethod.GET, "/orders/toConfirm").hasAuthority(Role.WAITER.toString())
                        .requestMatchers(HttpMethod.POST, "/order/confirm").hasAuthority(Role.WAITER.toString())
                        .requestMatchers(HttpMethod.POST,"/order/cancel").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET, "/orders").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults())     //to send basic auth in http
                .formLogin(Customizer.withDefaults())    //for default login form
                .csrf(AbstractHttpConfigurer::disable); // for POST requests via Postman;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
