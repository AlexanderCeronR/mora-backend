package mora.repository;

import mora.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(String categoria);
    // SELECT * FROM productos WHERE categoria = ?

    List<Producto> findByDisponibleTrue();
    // SELECT * FROM productos WHERE disponible = true
    // solo muestra productos disponibles al mesero

    List<Producto> findByCategoriaAndDisponibleTrue(String categoria);
    // SELECT * FROM productos WHERE categoria = ? AND disponible = true
    // filtra por categoría y que estén disponibles
}