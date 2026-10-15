package mora.repository;

import mora.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    List<Pago> findByMetodoPago(Pago.MetodoPago metodoPago);

    @EntityGraph(attributePaths = {
            "pedido",
            "pedido.detalles",
            "pedido.detalles.producto",
            "pedido.mesa",
            "pedido.mesero"
    })
    Optional<Pago> findById(Long id);
    // Carga el pago con todos sus relacionados
    // para generar la factura correctamente
}