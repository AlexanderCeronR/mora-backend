package mora.entity;

import com.fasterxml.jackson.annotation.JsonCreator; // Importación añadida
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(unique = true)
    private String numeroFactura;
    // Ej: MORA-2026-0001

    @Column
    private String cufe;
    // Código único de factura electrónica
    // cuando se conecte a la DIAN

    @PrePersist
    public void prePersist() {
        this.fechaPago = LocalDateTime.now();
    }

    public enum MetodoPago {
        EFECTIVO, TARJETA, TRANSFERENCIA;

        @JsonCreator
        public static MetodoPago fromString(String value) {
            if (value == null) {
                return null;
            }
            for (MetodoPago metodo : MetodoPago.values()) {
                if (metodo.name().equalsIgnoreCase(value)) {
                    return metodo;
                }
            }
            throw new IllegalArgumentException("Método de pago no válido: " + value);
        }
    }
} // Llave de cierre de la clase Pago añadida