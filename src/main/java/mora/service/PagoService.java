package mora.service;

import mora.dto.request.PagoRequest;
import mora.entity.Pago;
import mora.entity.Pedido;
import mora.repository.PagoRepository;
import mora.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoService(PagoRepository pagoRepository,
                       PedidoRepository pedidoRepository) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    // Trae todos los pagos
    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    // Trae un pago por id
    @Transactional(readOnly = true)
    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con id: " + id));
    }

    // Trae el pago de un pedido específico
    @Transactional(readOnly = true)
    public Pago obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException(
                        "No existe pago para el pedido: " + pedidoId));
    }

    // Registra el pago de un pedido
    @Transactional
    public Pago registrarPago(PagoRequest request) {

        // 1. Busca el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        // 2. Verifica que el pedido no esté ya pagado
        if (pedido.getEstado() == Pedido.Estado.PAGADO) {
            throw new RuntimeException(
                    "El pedido ya fue pagado anteriormente");
        }

        // 3. Verifica que el pedido esté entregado
        // no puedes cobrar algo que no se ha entregado
        if (pedido.getEstado() != Pedido.Estado.ENTREGADO) {
            throw new RuntimeException(
                    "El pedido debe estar ENTREGADO antes de pagar. " +
                            "Estado actual: " + pedido.getEstado());
        }

        // 4. Verifica que no exista ya un pago para este pedido
        pagoRepository.findByPedidoId(pedido.getId())
                .ifPresent(p -> {
                    throw new RuntimeException(
                            "Ya existe un pago para este pedido");
                });

        // 5. Crea el pago con el total del pedido
        Pago pago = Pago.builder()
                .pedido(pedido)
                .total(pedido.getTotal())
                // El total lo tomamos del pedido
                // no lo manda el frontend para evitar fraudes
                .metodoPago(request.getMetodoPago())
                .build();
        // @PrePersist asigna fechaPago automáticamente

        // 6. Cambia el estado del pedido a PAGADO
        pedido.setEstado(Pedido.Estado.PAGADO);
        pedidoRepository.save(pedido);

        // 7. Guarda y retorna el pago
        return pagoRepository.save(pago);
    }
}