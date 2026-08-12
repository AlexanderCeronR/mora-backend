package mora.repository;

import mora.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByEstado(Mesa.Estado estado);
    // SELECT * FROM mesas WHERE estado = ?
    // útil para ver todas las mesas libres de un vistazo

    Optional<Mesa> findByNumero(Integer numero);
    // SELECT * FROM mesas WHERE numero = ?
    // útil para buscar una mesa por su número visible
}