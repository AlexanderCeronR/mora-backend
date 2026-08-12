package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.ProductoRequest;
import mora.dto.response.ProductoResponse;
import mora.entity.Producto;
import mora.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        List<ProductoResponse> productos = productoService.obtenerTodos()
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    // Lo que ve el mesero cuando crea un pedido
    public ResponseEntity<List<ProductoResponse>> obtenerDisponibles() {
        List<ProductoResponse> productos = productoService.obtenerDisponibles()
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoria(
            @PathVariable String categoria) {
        List<ProductoResponse> productos = productoService
                .obtenerDisponiblesPorCategoria(categoria)
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ProductoResponse.fromEntity(
                        productoService.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    // Solo el ADMIN puede crear productos
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .categoria(request.getCategoria())
                .disponible(request.getDisponible() != null
                        ? request.getDisponible() : true)
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductoResponse.fromEntity(
                        productoService.crear(producto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .categoria(request.getCategoria())
                .disponible(request.getDisponible())
                .build();
        return ResponseEntity.ok(
                ProductoResponse.fromEntity(
                        productoService.actualizar(id, producto)));
    }

    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    // Si se acaba un plato el admin lo desactiva
    // sin necesidad de borrarlo
    public ResponseEntity<ProductoResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(
                ProductoResponse.fromEntity(
                        productoService.cambiarDisponibilidad(id, disponible)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}