package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.LoginRequest;
import mora.dto.request.RegisterRequest;
import mora.dto.response.AuthResponse;
import mora.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
// Todos los endpoints de esta clase empiezan con /auth
// Permite peticiones desde el frontend React
// en producción cambiarías * por la URL del frontend
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        // @Valid activa las validaciones del DTO
        // @RequestBody lee el JSON del request
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
// ← solo ADMIN puede crear usuarios
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}