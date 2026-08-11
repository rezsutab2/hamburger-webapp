package hu.hamburger.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//Minden HTTP kérésnél lefut és megvizsgálja van-e érvényes JWT token a kérésben
//A OncePerRequestFilter osztály biztosítja, hogy egyszer fusson csak le HTTP kérés esetén a kód
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    //minden kérésnél lefut
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        //A JWT tokenből kiveszi a headert, ami így szokott kinézni -> Authorization: Bearer efyJdfGci...
        String authHeader = request.getHeader("Authorization");
        //Ha van token és a maradék így kezdődik, hogy "Bearer " akkor substring-gel kivágjuk azt és
        //marad a token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            //Érvényes token esetén kiveszi belőle az email-t és betölti a felhasználót az adatbázisból
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                //Beállítja a Spring Security viszonylatában, hogy a felhasználó be van jelentkezve.
                //Ezután a Spring Security már tudni fogja ki küldte a kérést.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        //Továbbengedi a kérést a következő filter felé, majd végül a controllerhez.
        filterChain.doFilter(request, response);
    }
}