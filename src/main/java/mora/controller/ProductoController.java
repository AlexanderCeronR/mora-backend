package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.ProductoRequest;
import mora.dto.response.ProductoResponse;
import mora.entity.Producto;
import mora.service.CloudinaryService;
import mora.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CloudinaryService cloudinaryService;

    public ProductoController(ProductoService productoService,
                              CloudinaryService cloudinaryService) {
        this.productoService = productoService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO', 'SUPERVISOR')")
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        return ResponseEntity.ok(
                productoService.obtenerTodos()
                        .stream()
                        .map(ProductoResponse::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO', 'SUPERVISOR')")
    public ResponseEntity<List<ProductoResponse>> obtenerDisponibles() {
        return ResponseEntity.ok(
                productoService.obtenerDisponibles()
                        .stream()
                        .map(ProductoResponse::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO', 'SUPERVISOR')")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoria(
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                productoService.obtenerDisponiblesPorCategoria(categoria)
                        .stream()
                        .map(ProductoResponse::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO', 'SUPERVISOR')")
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ProductoResponse.fromEntity(
                        productoService.obtenerPorId(id))
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
                        productoService.actualizar(id, producto))
        );
    }

    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(
                ProductoResponse.fromEntity(
                        productoService.cambiarDisponibilidad(id, disponible))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/imagen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> subirImagen(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile file) {

        Producto producto = productoService.obtenerPorId(id);

        try {
            String url = cloudinaryService.subirImagen(file);
            producto.setImagenUrl(url);
            productoService.guardar(producto);

            return ResponseEntity.ok(Map.of(
                    "imagenUrl", url,
                    "mensaje", "Imagen subida correctamente"
            ));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al subir la imagen: " + e.getMessage());
        }
    }
}