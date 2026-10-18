package mora.controller;

import mora.entity.Pedido;
import mora.entity.Usuario;
import mora.repository.PedidoRepository;
import mora.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/supervisor")
@CrossOrigin(origins = "*")
public class SupervisorController {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public SupervisorController(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Ver pedidos nuevos sin asignar
    @GetMapping("/pedidos/nuevos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> pedidosNuevos() {
        return ResponseEntity.ok(
                pedidoRepository
                        .findByEstadoAsignacion(Pedido.EstadoAsignacion.NUEVO)
                        .stream()
                        .map(p -> Map.of(
                                "id", p.getId(),
                                "mesa", p.getMesa().getNumero(),
                                "total", p.getTotal(),
                                "fechaCreacion", p.getFechaCreacion().toString(),
                                "detalles", p.getDetalles().stream()
                                        .map(d -> Map.of(
                                                "producto", d.getProducto().getNombre(),
                                                "cantidad", d.getCantidad()
                                        )).toList()
                        )).toList()
        );
    }

    // Ver meseros disponibles
    @GetMapping("/meseros/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> meserosDisponibles() {
        return ResponseEntity.ok(
                usuarioRepository
                        .findByRolAndDisponibleTrue(Usuario.Rol.MESERO)
                        .stream()
                        .map(m -> {
                            long pedidosActivos = pedidoRepository
                                    .findByMeseroIdAndEstadoNot(
                                            m.getId(), Pedido.Estado.PAGADO)
                                    .size();
                            return Map.of(
                                    "id", (Object) m.getId(),
                                    "nombre", m.getNombre(),
                                    "pedidosActivos", pedidosActivos
                            );
                        }).toList()
        );
    }

    // Asignar mesero a un pedido
    @PatchMapping("/pedidos/{pedidoId}/asignar/{meseroId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> asignarMesero(
            @PathVariable Long pedidoId,
            @PathVariable Long meseroId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        Usuario mesero = usuarioRepository.findById(meseroId)
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        pedido.setMesero(mesero);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);
        pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pedido #" + pedidoId +
                        " asignado a " + mesero.getNombre(),
                "pedidoId", pedidoId,
                "mesero", mesero.getNombre()
        ));
    }

    // Dashboard del supervisor
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> dashboard() {
        List<Usuario> meseros = usuarioRepository
                .findByRol(Usuario.Rol.MESERO);

        List<Map<String, Object>> actividadMeseros = meseros.stream()
                .map(m -> {
                    List<Pedido> pedidosActivos = pedidoRepository
                            .findByMeseroIdAndEstadoNot(
                                    m.getId(), Pedido.Estado.PAGADO);

                    List<Map<String, Object>> pedidosMap = new ArrayList<>();
                    for (Pedido p : pedidosActivos) {
                        pedidosMap.add(Map.of(
                                "id", (Object) p.getId(),
                                "mesa", p.getMesa().getNumero(),
                                "estado", p.getEstado().name(),
                                "total", p.getTotal()
                        ));
                    }

                    return Map.of(
                            "id", (Object) m.getId(),
                            "nombre", m.getNombre(),
                            "disponible", m.getDisponible(),
                            "pedidosActivos", pedidosActivos.size(),
                            "pedidos", pedidosMap
                    );
                }).toList();

        long pedidosSinAsignar = pedidoRepository
                .findByEstadoAsignacion(Pedido.EstadoAsignacion.NUEVO)
                .size();

        return ResponseEntity.ok(Map.of(
                "pedidosSinAsignar", pedidosSinAsignar,
                "totalMeseros", meseros.size(),
                "meserosDisponibles", meseros.stream()
                        .filter(Usuario::getDisponible).count(),
                "actividadMeseros", actividadMeseros
        ));
    }

    // Cambiar disponibilidad de un mesero
    @PatchMapping("/meseros/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {

        Usuario mesero = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        mesero.setDisponible(disponible);
        usuarioRepository.save(mesero);

        return ResponseEntity.ok(Map.of(
                "mensaje", mesero.getNombre() + " ahora está " +
                        (disponible ? "disponible" : "no disponible"),
                "disponible", disponible
        ));
    }
    // Reasignar pedido a otro mesero
    @PatchMapping("/pedidos/{pedidoId}/reasignar/{meseroId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> reasignarMesero(
            @PathVariable Long pedidoId,
            @PathVariable Long meseroId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        Usuario mesero = usuarioRepository.findById(meseroId)
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        String meseroAnterior = pedido.getMesero() != null
                ? pedido.getMesero().getNombre()
                : "Sin asignar";

        pedido.setMesero(mesero);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);
        pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pedido #" + pedidoId +
                        " reasignado de " + meseroAnterior +
                        " a " + mesero.getNombre(),
                "pedidoId", pedidoId,
                "meseroAnterior", meseroAnterior,
                "meseroNuevo", mesero.getNombre()
        ));
    }
}

