package mora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
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
    @JoinColumn(name = "mesero_id", nullable = false)
    private Usuario mesero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
    }

    public enum Estado {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, PAGADO
    }
}