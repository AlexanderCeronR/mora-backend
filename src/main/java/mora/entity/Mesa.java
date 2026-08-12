package mora.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.LIBRE;

    public enum Estado {
        LIBRE, OCUPADA, RESERVADA
    }
}