package mora.service;

import mora.dto.request.PagoRequest;
import mora.entity.Pago;
import mora.entity.Pedido;
import mora.repository.PagoRepository;
import mora.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Pago obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException(
                        "No existe pago para el pedido: " + pedidoId));
    }

    @Transactional
    public Pago registrarPago(PagoRequest request) {

        Pedido pedido = pedidoRepository
                .findById(request.getPedidoId())
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        if (pedido.getEstado() == Pedido.Estado.PAGADO) {
            throw new RuntimeException(
                    "El pedido ya fue pagado anteriormente");
        }

        if (pedido.getEstado() != Pedido.Estado.ENTREGADO) {
            throw new RuntimeException(
                    "El pedido debe estar ENTREGADO antes de pagar. " +
                            "Estado actual: " + pedido.getEstado());
        }

        pagoRepository.findByPedidoId(pedido.getId())
                .ifPresent(p -> {
                    throw new RuntimeException(
                            "Ya existe un pago para este pedido");
                });

        // Genera número de factura único
        String numeroFactura = generarNumeroFactura();

        Pago pago = Pago.builder()
                .pedido(pedido)
                .total(pedido.getTotal())
                .metodoPago(request.getMetodoPago())
                .numeroFactura(numeroFactura)
                .build();

        pedido.setEstado(Pedido.Estado.PAGADO);
        pedidoRepository.save(pedido);

        return pagoRepository.save(pago);
    }

    private String generarNumeroFactura() {
        // Formato: MORA-2026-0001
        String año = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy"));
        long totalPagos = pagoRepository.count() + 1;
        return String.format("MORA-%s-%04d", año, totalPagos);
    }
}