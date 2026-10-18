package mora.controller;

import mora.entity.Pedido;
import mora.repository.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cocina")
@CrossOrigin(origins = "*")
public class CocinaController {

    private final PedidoRepository pedidoRepository;

    public CocinaController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // Ver pedidos activos para cocina
    @GetMapping("/pedidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'COCINA', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> pedidosActivos() {
        List<Pedido> pedidos = pedidoRepository
                .findByEstado(Pedido.Estado.PENDIENTE);

        List<Pedido> enPreparacion = pedidoRepository
                .findByEstado(Pedido.Estado.EN_PREPARACION);

        pedidos.addAll(enPreparacion);

        return ResponseEntity.ok(
                pedidos.stream()
                        .map(p -> Map.of(
                                "id", (Object) p.getId(),
                                "mesa", p.getMesa().getNumero(),
                                "estado", p.getEstado().name(),
                                "mesero", p.getMesero() != null
                                        ? p.getMesero().getNombre()
                                        : "Sin asignar",
                                "fechaCreacion", p.getFechaCreacion().toString(),
                                "detalles", p.getDetalles().stream()
                                        .map(d -> Map.of(
                                                "producto", d.getProducto().getNombre(),
                                                "cantidad", (Object) d.getCantidad(),
                                                "notas", d.getNotas() != null
                                                        ? d.getNotas() : ""
                                        )).toList()
                        )).toList()
        );
    }

    // Avanzar estado desde cocina
    @Transactional
    @PatchMapping("/pedidos/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'COCINA', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Pedido.Estado estado) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        // Cocina solo puede avanzar a EN_PREPARACION o LISTO
        if (estado != Pedido.Estado.EN_PREPARACION &&
                estado != Pedido.Estado.LISTO) {
            throw new RuntimeException(
                    "Cocina solo puede cambiar a EN_PREPARACION o LISTO");
        }

        pedido.setEstado(estado);
        pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pedido #" + id + " → " + estado.name(),
                "pedidoId", id,
                "estado", estado.name()
        ));
    }

    // Ver pedidos LISTOS para que el mesero los recoja
    @GetMapping("/pedidos/listos")
    @PreAuthorize("hasAnyRole('ADMIN', 'COCINA', 'MESERO', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> pedidosListos() {
        return ResponseEntity.ok(
                pedidoRepository.findByEstado(Pedido.Estado.LISTO)
                        .stream()
                        .map(p -> Map.of(
                                "id", (Object) p.getId(),
                                "mesa", p.getMesa().getNumero(),
                                "mesero", p.getMesero() != null
                                        ? p.getMesero().getNombre()
                                        : "Sin asignar",
                                "fechaCreacion", p.getFechaCreacion().toString()
                        )).toList()
        );
    }
}