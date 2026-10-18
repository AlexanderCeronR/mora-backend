package mora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mora.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
// @Component le dice a Spring que maneje esta clase
// como un bean — puede inyectarse en otras clases
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(
            @Value("${mora.jwt.secret}") String secret,
            @Value("${mora.jwt.expiration}") long expiration) {
        // Convierte el String del application.properties
        // en una clave criptográfica segura
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    // Genera un token JWT con el email y rol del usuario
    public String generateToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                // subject = quién es el usuario (su email)
                .claim("rol", usuario.getRol().name())
                // claim = datos extra que metemos en el token
                .claim("nombre", usuario.getNombre())
                .issuedAt(new Date())
                // issuedAt = cuándo se creó el token
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // expiration = cuándo vence (ahora + 24 horas)
                .signWith(secretKey)
                // firma el token con nuestra clave secreta
                .compact();
        // genera el String final eyJhbGci...
    }

    // Extrae todos los datos del token
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extrae solo el email del token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Verifica si el token es válido y no ha vencido
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
            // after(new Date()) = la expiración es después de ahora
        } catch (Exception e) {
            return false;
            // si el token está mal formado o fue manipulado
            // lanza excepción y retornamos false
        }
    }
}