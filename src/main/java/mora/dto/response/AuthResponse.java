package mora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String token;
    // El JWT que el frontend guarda y manda en cada request

    private String nombre;
    private String email;
    private String rol;
    // Datos del usuario para que el frontend
    // sepa qué menú mostrar según el rol
}