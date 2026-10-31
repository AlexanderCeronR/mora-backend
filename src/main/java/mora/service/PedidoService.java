package mora.service;

import mora.dto.request.DetallePedidoRequest;
import mora.dto.request.PedidoRequest;
import mora.entity.*;
import mora.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         MesaRepository mesaRepository,
                         UsuarioRepository usuarioRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorEstado(Pedido.Estado estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorMesa(Long mesaId) {
        return pedidoRepository.findByMesaId(mesaId);
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + id));
    }

    @Transactional
    public Pedido crear(PedidoRequest request) {

        // 1. Mesa: debe existir y estar libre
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new RuntimeException(
                        "Mesa no encontrada"));

        if (mesa.getEstado() != Mesa.Estado.LIBRE) {
            throw new RuntimeException(
                    "La mesa " + mesa.getNumero() + " no está libre");
        }

        // 2. Responsable: solo mesero o supervisor
        Usuario mesero = usuarioRepository.findById(request.getMeseroId())
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        if (mesero.getRol() != Usuario.Rol.MESERO
                && mesero.getRol() != Usuario.Rol.SUPERVISOR
                && mesero.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException(
                    "Este usuario no puede atender pedidos");
        }

        // 3. Debe traer productos
        if (request.getDetalles() == null
                || request.getDetalles().isEmpty()) {
            throw new RuntimeException(
                    "El pedido debe tener al menos un producto");
        }

        // 4. Pedido base (con mesero queda ASIGNADO en @PrePersist)
        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .mesero(mesero)
                .estadoAsignacion(Pedido.EstadoAsignacion.ASIGNADO)
                .detalles(new ArrayList<>())
                .build();

        // 5. Detalles y total
        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequest detalleReq : request.getDetalles()) {
            if (detalleReq.getCantidad() == null
                    || detalleReq.getCantidad() < 1) {
                throw new RuntimeException(
                        "La cantidad debe ser al menos 1");
            }

            Producto producto = productoRepository
                    .findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: "
                                    + detalleReq.getProductoId()));

            if (!producto.getDisponible()) {
                throw new RuntimeException(
                        "El producto " + producto.getNombre()
                                + " no está disponible");
            }

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(detalleReq.getCantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(detalleReq.getCantidad())
                    .subtotal(subtotal)
                    .notas(detalleReq.getNotas())
                    .build();

            pedido.getDetalles().add(detalle);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        // 6. Mesa ocupada
        mesa.setEstado(Mesa.Estado.OCUPADA);
        mesaRepository.save(mesa);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, Pedido.Estado nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + id));

        pedido.setEstado(nuevoEstado);

        // La mesa se libera cuando el cliente ya pagó
        if (nuevoEstado == Pedido.Estado.PAGADO) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(Mesa.Estado.LIBRE);
            mesaRepository.save(mesa);
        }

        pedidoRepository.save(pedido);

        return pedidoRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = obtenerPorId(id);

        if (pedido.getEstado() == Pedido.Estado.PAGADO) {
            throw new RuntimeException(
                    "No se puede cancelar un pedido ya pagado");
        }
        if (pedido.getEstado() == Pedido.Estado.ENTREGADO) {
            throw new RuntimeException(
                    "No se puede cancelar un pedido ya entregado");
        }

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(Mesa.Estado.LIBRE);
        mesaRepository.save(mesa);

        pedidoRepository.delete(pedido);
    }
}