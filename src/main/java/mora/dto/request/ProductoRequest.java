package mora.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    private Boolean disponible = true;
}