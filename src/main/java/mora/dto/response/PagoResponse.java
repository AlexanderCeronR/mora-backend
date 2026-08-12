package mora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mora.entity.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoResponse {

    private Long id;
    private Long pedidoId;
    private Integer mesaNumero;
    private BigDecimal total;
    private String metodoPago;
    private LocalDateTime fechaPago;

    public static PagoResponse fromEntity(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .pedidoId(pago.getPedido().getId())
                .mesaNumero(pago.getPedido().getMesa().getNumero())
                .total(pago.getTotal())
                .metodoPago(pago.getMetodoPago().name())
                .fechaPago(pago.getFechaPago())
                .build();
    }
}