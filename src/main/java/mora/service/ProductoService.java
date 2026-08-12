package mora.service;

import mora.entity.Producto;
import mora.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Trae todos los productos
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Trae solo los disponibles — lo que ve el mesero
    @Transactional(readOnly = true)
    public List<Producto> obtenerDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }

    // Trae por categoría
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    // Trae disponibles por categoría
    @Transactional(readOnly = true)
    public List<Producto> obtenerDisponiblesPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndDisponibleTrue(categoria);
    }

    // Trae un producto por id
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con id: " + id));
    }

    // Crea un producto nuevo
    @Transactional
    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    // Actualiza un producto
    @Transactional
    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setCategoria(productoActualizado.getCategoria());
        producto.setDisponible(productoActualizado.getDisponible());
        return productoRepository.save(producto);
    }

    // Activa o desactiva un producto
    // en vez de borrar, lo marcamos como no disponible
    @Transactional
    public Producto cambiarDisponibilidad(Long id, Boolean disponible) {
        Producto producto = obtenerPorId(id);
        producto.setDisponible(disponible);
        return productoRepository.save(producto);
    }

    // Elimina un producto
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        productoRepository.delete(producto);
    }
}