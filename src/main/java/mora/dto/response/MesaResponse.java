package mora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mora.entity.Mesa;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesaResponse {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private String estado;

    // Convierte una entidad Mesa a MesaResponse
    // lo usamos en el service para no exponer
    // la entidad directamente desde el controller
    public static MesaResponse fromEntity(Mesa mesa) {
        return MesaResponse.builder()
                .id(mesa.getId())
                .numero(mesa.getNumero())
                .capacidad(mesa.getCapacidad())
                .estado(mesa.getEstado().name())
                .build();
    }
}