package mora.repository;

import mora.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);
    // Busca el pago de un pedido específico

    List<Pago> findByMetodoPago(Pago.MetodoPago metodoPago);
    // Ver pagos por método: EFECTIVO, TARJETA, TRANSFERENCIA
}