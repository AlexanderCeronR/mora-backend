package mora.controller;

import mora.entity.Usuario;
import mora.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearAUsuarioResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (body.containsKey("nombre") && body.get("nombre") != null) {
            usuario.setNombre(body.get("nombre"));
        }

        if (body.containsKey("email") && body.get("email") != null) {
            usuario.setEmail(body.get("email"));
        }

        if (body.containsKey("password") && body.get("password") != null && !body.get("password").isBlank()) {
            usuario.setPassword(passwordEncoder.encode(body.get("password")));
        }

        if (body.containsKey("rol") && body.get("rol") != null) {
            usuario.setRol(Usuario.Rol.valueOf(body.get("rol").toUpperCase()));
        }

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(mapearAUsuarioResponse(usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar seguro frente a valores null
    private Map<String, Object> mapearAUsuarioResponse(Usuario u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("nombre", u.getNombre());
        map.put("email", u.getEmail());
        map.put("rol", u.getRol() != null ? u.getRol().name() : null);
        return map;
    }
}