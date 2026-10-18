package mora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mora.entity.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PedidoResponse {

    private Long id;
    private Long mesaId;
    private Integer mesaNumero;
    private Long meseroId;
    private String meseroNombre;
    private String estado;
    private LocalDateTime fechaCreacion;
    private BigDecimal total;
    private List<DetallePedidoResponse> detalles;

    public static PedidoResponse fromEntity(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .mesaId(pedido.getMesa().getId())
                .mesaNumero(pedido.getMesa().getNumero())
                .meseroId(pedido.getMesero().getId())
                .meseroNombre(pedido.getMesero().getNombre())
                .estado(pedido.getEstado().name())
                .fechaCreacion(pedido.getFechaCreacion())
                .total(pedido.getTotal())
                .detalles(pedido.getDetalles()
                        .stream()
                        .map(DetallePedidoResponse::fromEntity)
                        .toList())
                .build();
    }
}