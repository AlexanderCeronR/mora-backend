package mora.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRequest {

    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;

    @NotNull(message = "El mesero es obligatorio")
    private Long meseroId;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<DetallePedidoRequest> detalles;
}