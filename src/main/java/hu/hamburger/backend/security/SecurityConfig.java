package hu.hamburger.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //Ez a metódus mondja meg a Spring Security-nek hogyan viselkedjen
        http
                .csrf(csrf -> csrf.disable())
                //A CSRF védelmet kikapcsoljuk – REST API-nál nincs rá szükség, mert JWT tokent használunk.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //Megmondjuk hogy ne használjon session-t – minden kérésnél a JWT token azonosítja a felhasználót.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/menu").permitAll()
                        .anyRequest().authenticated())
                //megmondja mely végpontok érhetők el bejelentkezés nélkül
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        //Berakja a JwtFilter-t a filterláncba – minden kérésnél lefut.

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //jelszó titkosítás
        return new BCryptPasswordEncoder();
        //egy erős titkosítási algoritmus – a jelszavakat soha nem szabad nyersen tárolni az adatbázisban.
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        //bejelentkezés kezelője
    }
}