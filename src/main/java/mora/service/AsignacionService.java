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

        // 1. Busca todos los meseros disponibles
        List<Usuario> meserosDisponibles = usuarioRepository
                .findByRolAndDisponibleTrue(Usuario.Rol.MESERO);

        if (meserosDisponibles.isEmpty()) {
            // No hay meseros disponibles
            // el pedido queda en NUEVO para asignación manual
            return Optional.empty();
        }

        // 2. Cuenta pedidos activos por mesero
        // y elige el que menos tenga — balanceo de carga
        Usuario meseroAsignado = meserosDisponibles.stream()
                .min(Comparator.comparingLong(mesero ->
                        pedidoRepository
                                .findByMeseroIdAndEstadoNot(
                                        mesero.getId(),
                                        Pedido.Estado.PAGADO)
                                .size()
                ))
                .orElse(meserosDisponibles.get(0));

        // 3. Asigna el mesero al pedido
        pedido.setMesero(meseroAsignado);
        pedido.setEstadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO);

        return Optional.of(meseroAsignado);
    }
}