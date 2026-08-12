package mora.repository;

import mora.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    List<Pedido> findAll();
    // Carga todos los pedidos con sus detalles en una sola query
    // evita el LazyInitializationException

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    List<Pedido> findByEstado(Pedido.Estado estado);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    List<Pedido> findByMesaId(Long mesaId);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    List<Pedido> findByMeseroId(Long meseroId);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "mesa", "mesero"})
    List<Pedido> findByEstadoNot(Pedido.Estado estado);
}