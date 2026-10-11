package hu.hamburger.backend.controller;

import hu.hamburger.backend.model.Role;
import hu.hamburger.backend.model.User;
import hu.hamburger.backend.repository.UserRepository;
import hu.hamburger.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return "Sikeres regisztráció!";
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.get("email"),
                        credentials.get("password")));

        User user = userRepository.findByEmail(credentials.get("email"))
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található"));

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}