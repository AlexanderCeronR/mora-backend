package mora.service;

import mora.dto.request.LoginRequest;
import mora.dto.request.RegisterRequest;
import mora.dto.response.AuthResponse;
import mora.entity.Usuario;
import mora.repository.UsuarioRepository;
import mora.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Verifica email y password contra la BD
        // Si algo está mal lanza excepción automáticamente
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // 2. Busca el usuario en la BD
        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Genera el token JWT
        String token = jwtUtil.generateToken(usuario);

        // 4. Retorna el token + datos del usuario
        return AuthResponse.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Crea el nuevo usuario
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // encode() hashea el password con BCrypt
                // nunca guardamos el password en texto plano
                .rol(request.getRol() != null
                        ? request.getRol()
                        : Usuario.Rol.MESERO)
                // Si no mandan rol, por defecto es MESERO
                .build();

        // 2. Guarda en la BD
        usuarioRepository.save(usuario);

        // 3. Genera el token y retorna
        String token = jwtUtil.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build();
    }
}