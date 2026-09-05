package mora.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mora.entity.Pago;
import com.fasterxml.jackson.annotation.JsonCreator;

@Data
public class PagoRequest {

    @NotNull(message = "El pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El método de pago es obligatorio")
    private Pago.MetodoPago metodoPago;
}