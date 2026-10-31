package mora.controller;

import mora.entity.Pedido;
import mora.entity.Usuario;
import mora.repository.PedidoRepository;
import mora.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Solo meseros y supervisores pueden recibir pedidos
    private void validarReceptor(Usuario usuario) {
        if (usuario.getRol() != Usuario.Rol.MESERO
                && usuario.getRol() != Usuario.Rol.SUPERVISOR) {
            throw new RuntimeException(
                    "Solo meseros o supervisores pueden recibir pedidos");
        }
        if (!Boolean.TRUE.equals(usuario.getDisponible())) {
            throw new RuntimeException("El usuario no está disponible");
        }
    }

    // Ver pedidos nuevos sin asignar
    @GetMapping("/pedidos/nuevos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> pedidosNuevos() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Pedido p : pedidoRepository.findByEstadoAsignacion(
                Pedido.EstadoAsignacion.NUEVO)) {

            // Solo pedidos sin mesero y que aún no se pagaron
            if (p.getMesero() != null) continue;
            if (p.getEstado() == Pedido.Estado.PAGADO) continue;

            List<Map<String, Object>> detalles = new ArrayList<>();
            p.getDetalles().forEach(d -> {
                Map<String, Object> det = new HashMap<>();
                det.put("producto", d.getProducto().getNombre());
                det.put("cantidad", d.getCantidad());
                detalles.add(det);
            });

            Map<String, Object> fila = new HashMap<>();
            fila.put("id", p.getId());
            fila.put("mesa", p.getMesa().getNumero());
            fila.put("total", p.getTotal());
            fila.put("fechaCreacion", p.getFechaCreacion().toString());
            fila.put("detalles", detalles);
            resultado.add(fila);
        }

        return ResponseEntity.ok(resultado);
    }

    // Ver meseros y supervisores disponibles
    @GetMapping("/meseros/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> meserosDisponibles() {

        List<Usuario> disponibles = new ArrayList<>();
        disponibles.addAll(
                usuarioRepository.findByRolAndDisponibleTrue(Usuario.Rol.MESERO));
        disponibles.addAll(
                usuarioRepository.findByRolAndDisponibleTrue(Usuario.Rol.SUPERVISOR));

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Usuario m : disponibles) {
            long activos = pedidoRepository
                    .findByMeseroIdAndEstadoNot(m.getId(), Pedido.Estado.PAGADO)
                    .size();
            Map<String, Object> fila = new HashMap<>();
            fila.put("id", m.getId());
            fila.put("nombre", m.getNombre() +
                    (m.getRol() == Usuario.Rol.SUPERVISOR ? " (Supervisor)" : ""));
            fila.put("pedidosActivos", activos);
            resultado.add(fila);
        }

        return ResponseEntity.ok(resultado);
    }

    // Asignar mesero a un pedido
    @PatchMapping("/pedidos/{pedidoId}/asignar/{meseroId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> asignarMesero(
            @PathVariable Long pedidoId,
            @PathVariable Long meseroId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        Usuario mesero = usuarioRepository.findById(meseroId)
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        validarReceptor(mesero);

        pedido.setMesero(mesero);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);
        pedidoRepository.save(pedido);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Pedido #" + pedidoId +
                " asignado a " + mesero.getNombre());
        resp.put("pedidoId", pedidoId);
        resp.put("mesero", mesero.getNombre());
        return ResponseEntity.ok(resp);
    }

    // Dashboard del supervisor
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> dashboard() {
        List<Usuario> personal = new ArrayList<>();
        personal.addAll(usuarioRepository.findByRol(Usuario.Rol.MESERO));
        personal.addAll(usuarioRepository.findByRol(Usuario.Rol.SUPERVISOR));

        List<Map<String, Object>> actividad = new ArrayList<>();
        for (Usuario m : personal) {
            List<Pedido> activos = pedidoRepository
                    .findByMeseroIdAndEstadoNot(
                            m.getId(), Pedido.Estado.PAGADO);

            List<Map<String, Object>> pedidosMap = new ArrayList<>();
            for (Pedido p : activos) {
                Map<String, Object> pm = new HashMap<>();
                pm.put("id", p.getId());
                pm.put("mesa", p.getMesa().getNumero());
                pm.put("estado", p.getEstado().name());
                pm.put("total", p.getTotal());
                pedidosMap.add(pm);
            }

            Map<String, Object> fila = new HashMap<>();
            fila.put("id", m.getId());
            fila.put("nombre", m.getNombre() +
                    (m.getRol() == Usuario.Rol.SUPERVISOR ? " (Supervisor)" : ""));
            fila.put("disponible", m.getDisponible());
            fila.put("pedidosActivos", activos.size());
            fila.put("pedidos", pedidosMap);
            actividad.add(fila);
        }

        long sinAsignar = pedidoRepository
                .findByEstadoAsignacion(Pedido.EstadoAsignacion.NUEVO)
                .size();

        Map<String, Object> resp = new HashMap<>();
        resp.put("pedidosSinAsignar", sinAsignar);
        resp.put("totalMeseros", personal.size());
        resp.put("meserosDisponibles", personal.stream()
                .filter(u -> Boolean.TRUE.equals(u.getDisponible())).count());
        resp.put("actividadMeseros", actividad);
        return ResponseEntity.ok(resp);
    }

    // Cambiar disponibilidad de un mesero
    @PatchMapping("/meseros/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {

        Usuario mesero = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        mesero.setDisponible(disponible);
        usuarioRepository.save(mesero);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", mesero.getNombre() + " ahora está " +
                (disponible ? "disponible" : "no disponible"));
        resp.put("disponible", disponible);
        return ResponseEntity.ok(resp);
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

        validarReceptor(mesero);

        String meseroAnterior = pedido.getMesero() != null
                ? pedido.getMesero().getNombre()
                : "Sin asignar";

        pedido.setMesero(mesero);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);
        pedidoRepository.save(pedido);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Pedido #" + pedidoId +
                " reasignado de " + meseroAnterior +
                " a " + mesero.getNombre());
        resp.put("pedidoId", pedidoId);
        resp.put("meseroAnterior", meseroAnterior);
        resp.put("meseroNuevo", mesero.getNombre());
        return ResponseEntity.ok(resp);
    }
}