package mora.repository;

import mora.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findAll();

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findByEstado(Pedido.Estado estado);

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findByMesaId(Long mesaId);

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findByMeseroId(Long meseroId);

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findByEstadoAsignacion(
            Pedido.EstadoAsignacion estadoAsignacion);
    // Trae pedidos nuevos sin asignar

    @EntityGraph(attributePaths = {
            "detalles", "detalles.producto", "mesa", "mesero"
    })
    List<Pedido> findByMeseroIdAndEstadoNot(
            Long meseroId, Pedido.Estado estado);
    // Pedidos activos de un mesero
    // excluye los pagados

    Optional<Pedido> findByTokenCliente(String tokenCliente);
    // El cliente busca su pedido con su token
}