package mora.service;

import mora.entity.Pedido;
import mora.entity.Usuario;
import mora.repository.PedidoRepository;
import mora.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public AsignacionService(
            UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Optional<Usuario> asignarMeseroAutomatico(Pedido pedido) {

        // 1. Meseros disponibles (prioridad)
        List<Usuario> candidatos = usuarioRepository
                .findByRolAndDisponibleTrue(Usuario.Rol.MESERO);

        // 2. Contingencia: si no hay meseros, usa supervisores
        if (candidatos.isEmpty()) {
            candidatos = usuarioRepository
                    .findByRolAndDisponibleTrue(Usuario.Rol.SUPERVISOR);
        }

        // 3. Nadie disponible: queda en NUEVO para asignación manual
        if (candidatos.isEmpty()) {
            return Optional.empty();
        }

        // 4. Balanceo de carga: el que menos pedidos activos tenga
        Usuario asignado = candidatos.stream()
                .min(Comparator.comparingLong(u ->
                        pedidoRepository
                                .findByMeseroIdAndEstadoNot(
                                        u.getId(),
                                        Pedido.Estado.PAGADO)
                                .size()
                ))
                .orElse(candidatos.get(0));

        pedido.setMesero(asignado);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);

        return Optional.of(asignado);
    }
}