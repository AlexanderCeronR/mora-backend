package mora.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = se ejecuta una sola vez
    // por cada request HTTP que llega al servidor

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lee el header Authorization del request
        String authHeader = request.getHeader("Authorization");

        // 2. Si no hay header o no empieza con "Bearer "
        //    deja pasar el request sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrae el token quitando "Bearer " del inicio
        String token = authHeader.substring(7);

        // 4. Valida el token
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extrae el email del token
        String email = jwtUtil.extractEmail(token);

        // 6. Carga el usuario desde la base de datos
        UserDetails userDetails = userDetailsService
                .loadUserByUsername(email);

        // 7. Crea el objeto de autenticación y lo mete
        //    en el contexto de Spring Security
        //    A partir de aquí Spring sabe quién es el usuario
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        authToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request));

        SecurityContextHolder.getContext()
                .setAuthentication(authToken);

        // 8. Continúa con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}