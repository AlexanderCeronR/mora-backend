package mora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mora.entity.Producto;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private Boolean disponible;
    private String imagenUrl;

// En el método fromEntity:


    public static ProductoResponse fromEntity(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .categoria(producto.getCategoria())
                .disponible(producto.getDisponible())
                .imagenUrl(producto.getImagenUrl())
                .build();
    }
}