package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.PedidoRequest;
import mora.dto.response.PedidoResponse;
import mora.entity.Pedido;
import mora.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<List<PedidoResponse>> obtenerTodos() {
        List<PedidoResponse> pedidos = pedidoService.obtenerTodos()
                .stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    // Útil para la vista de cocina
    // GET /pedidos/estado/PENDIENTE
    public ResponseEntity<List<PedidoResponse>> obtenerPorEstado(
            @PathVariable Pedido.Estado estado) {
        List<PedidoResponse> pedidos = pedidoService
                .obtenerPorEstado(estado)
                .stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/mesa/{mesaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<List<PedidoResponse>> obtenerPorMesa(
            @PathVariable Long mesaId) {
        List<PedidoResponse> pedidos = pedidoService
                .obtenerPorMesa(mesaId)
                .stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<PedidoResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                PedidoResponse.fromEntity(
                        pedidoService.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    // El mesero crea el pedido
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody PedidoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PedidoResponse.fromEntity(
                        pedidoService.crear(request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    // Cambia estado: PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Pedido.Estado estado) {
        return ResponseEntity.ok(
                PedidoResponse.fromEntity(
                        pedidoService.cambiarEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}