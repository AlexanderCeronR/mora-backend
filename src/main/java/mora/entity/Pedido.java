package mora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "mesero_id")
    private Usuario mesero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoAsignacion estadoAsignacion = EstadoAsignacion.NUEVO;

    @Column(unique = true)
    private String tokenCliente;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
        this.estadoAsignacion = EstadoAsignacion.NUEVO;
        this.tokenCliente = UUID.randomUUID().toString();
    }

    public enum Estado {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, PAGADO
    }

    public enum EstadoAsignacion {
        NUEVO, ASIGNADO
    }
}