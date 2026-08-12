package mora.controller;

import mora.repository.PagoRepository;
import mora.repository.PedidoRepository;
import mora.repository.ProductoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public ReporteController(PagoRepository pagoRepository,
                             PedidoRepository pedidoRepository,
                             ProductoRepository productoRepository) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public Map<String, Object> resumen() {
        Map<String, Object> data = new HashMap<>();

        // Total ventas
        BigDecimal totalVentas = pagoRepository.findAll()
                .stream()
                .map(p -> p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total pedidos
        long totalPedidos = pedidoRepository.count();

        // Total pagos
        long totalPagos = pagoRepository.count();

        // Ventas por método de pago
        Map<String, Long> porMetodo = pagoRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMetodoPago().name(),
                        Collectors.counting()));

        // Productos más vendidos
        Map<String, Long> productosMasVendidos = pedidoRepository
                .findAll()
                .stream()
                .flatMap(p -> p.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getNombre(),
                        Collectors.summingLong(
                                d -> d.getCantidad().longValue())));

        // Ventas por categoría
        Map<String, BigDecimal> porCategoria = pedidoRepository
                .findAll()
                .stream()
                .flatMap(p -> p.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getCategoria(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                d -> d.getSubtotal(),
                                BigDecimal::add)));

        data.put("totalVentas", totalVentas);
        data.put("totalPedidos", totalPedidos);
        data.put("totalPagos", totalPagos);
        data.put("porMetodoPago", porMetodo);
        data.put("productosMasVendidos", productosMasVendidos);
        data.put("porCategoria", porCategoria);

        return data;
    }
}