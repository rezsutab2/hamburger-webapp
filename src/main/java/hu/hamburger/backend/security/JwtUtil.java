package hu.hamburger.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

//Token létrehozásáért és ellenőrzéséért felelős osztály
@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); //titkos kulcs a tokenhez
    private final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 óra alatt jár le a token

    public String generateToken(String email) { //Maga a tokengenerálás email-címhez rendelve
        return Jwts.builder()
                .setSubject(email) //kinek szól a token
                .setIssuedAt(new Date()) //mikor lett létrehozva
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) //mikor jár le
                .signWith(key) //aláírás a titkos kulccsal
                .compact(); //összecsomagolás String-gé
    }

    public String extractEmail(String token) { //Beérkező kérésből az tokent vizsgálva azonosítja az emailt
                                                //Így tudjuk, hogy ki küldte a kérést
        return Jwts.parserBuilder()
                .setSigningKey(key) //kulcs alapján ellenőriz
                .build()
                .parseClaimsJws(token) //kibontja a tokent
                .getBody()
                .getSubject(); //kiszedi belőle az email-t az azonosításhoz
    }

    public boolean validateToken(String token) { //token érvényességének ellenőrzése. Hiba esetén false-t dob
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}