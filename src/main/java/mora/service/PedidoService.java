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

    // Trae todos los pedidos
    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    // Trae pedidos por estado
    // útil para la vista de cocina
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorEstado(Pedido.Estado estado) {
        return pedidoRepository.findByEstado(estado);
    }

    // Trae pedidos de una mesa
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorMesa(Long mesaId) {
        return pedidoRepository.findByMesaId(mesaId);
    }

    // Trae un pedido por id
    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + id));
    }

    // Crea un pedido completo con sus detalles
    @Transactional
    public Pedido crear(PedidoRequest request) {

        // 1. Busca la mesa y verifica que esté libre
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new RuntimeException(
                        "Mesa no encontrada"));

        if (mesa.getEstado() == Mesa.Estado.OCUPADA) {
            throw new RuntimeException(
                    "La mesa " + mesa.getNumero() + " ya está ocupada");
        }

        // 2. Busca el mesero
        Usuario mesero = usuarioRepository.findById(request.getMeseroId())
                .orElseThrow(() -> new RuntimeException(
                        "Mesero no encontrado"));

        // 3. Crea el pedido base
        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .mesero(mesero)
                .detalles(new ArrayList<>())
                .build();
        // @PrePersist asigna estado PENDIENTE y fechaCreacion

        // 4. Procesa cada detalle y calcula subtotales
        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequest detalleReq : request.getDetalles()) {
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

            // subtotal = precio × cantidad
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

        // 5. Asigna el total al pedido
        pedido.setTotal(total);

        // 6. Cambia el estado de la mesa a OCUPADA
        mesa.setEstado(Mesa.Estado.OCUPADA);
        mesaRepository.save(mesa);

        // 7. Guarda el pedido con todos sus detalles
        return pedidoRepository.save(pedido);
    }

    // Cambia el estado del pedido
    @Transactional
    public Pedido cambiarEstado(Long id, Pedido.Estado nuevoEstado) {
        // Usa findById del repository que tiene @EntityGraph
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + id));

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == Pedido.Estado.ENTREGADO) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(Mesa.Estado.LIBRE);
            mesaRepository.save(mesa);
        }

        pedidoRepository.save(pedido);

        // Recarga el pedido con todos sus detalles
        return pedidoRepository.findById(id)
                .orElseThrow();
    }

    // Cancela un pedido
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = obtenerPorId(id);

        if (pedido.getEstado() == Pedido.Estado.PAGADO) {
            throw new RuntimeException(
                    "No se puede cancelar un pedido ya pagado");
        }

        // Libera la mesa
        Mesa mesa = pedido.getMesa();
        mesa.setEstado(Mesa.Estado.LIBRE);
        mesaRepository.save(mesa);

        pedidoRepository.delete(pedido);
    }
}