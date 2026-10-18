package mora.controller;

import mora.dto.request.DetallePedidoRequest;
import mora.dto.response.ProductoResponse;
import mora.entity.*;
import mora.repository.*;
import mora.service.AsignacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/publico")
@CrossOrigin(origins = "*")
public class PublicoController {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final AsignacionService asignacionService;

    public PublicoController(
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            MesaRepository mesaRepository,
            AsignacionService asignacionService) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.asignacionService = asignacionService;
    }

    @GetMapping("/menu")
    public ResponseEntity<List<ProductoResponse>> verMenu() {
        List<ProductoResponse> productos = productoRepository
                .findByDisponibleTrue()
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/menu/{categoria}")
    public ResponseEntity<List<ProductoResponse>> verMenuPorCategoria(
            @PathVariable String categoria) {
        List<ProductoResponse> productos = productoRepository
                .findByCategoriaAndDisponibleTrue(categoria)
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @PostMapping("/mesa/{mesaId}/pedido")
    public ResponseEntity<Map<String, Object>> hacerPedido(
            @PathVariable Long mesaId,
            @RequestBody List<DetallePedidoRequest> detalles) {

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException(
                        "Mesa no encontrada"));

        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .mesero(null)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequest req : detalles) {
            Producto producto = productoRepository
                    .findById(req.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado"));

            if (!producto.getDisponible()) {
                throw new RuntimeException(
                        "Producto no disponible: " +
                                producto.getNombre());
            }

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(req.getCantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(req.getCantidad())
                    .subtotal(subtotal)
                    .notas(req.getNotas())
                    .build();

            pedido.getDetalles().add(detalle);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        // Intenta asignación automática
        Optional<Usuario> meseroAsignado =
                asignacionService.asignarMeseroAutomatico(pedido);

        // Cambia la mesa a OCUPADA
        mesa.setEstado(Mesa.Estado.OCUPADA);
        mesaRepository.save(mesa);

        Pedido guardado = pedidoRepository.save(pedido);

        String mensajeAsignacion = meseroAsignado
                .map(m -> "Tu pedido fue asignado a " + m.getNombre())
                .orElse("Tu pedido fue recibido. " +
                        "Un mesero será asignado pronto.");

        return ResponseEntity.ok(Map.of(
                "pedidoId", guardado.getId(),
                "tokenCliente", guardado.getTokenCliente(),
                "total", guardado.getTotal(),
                "meseroAsignado", meseroAsignado
                        .map(Usuario::getNombre)
                        .orElse("Pendiente"),
                "mensaje", mensajeAsignacion
        ));
    }

    @GetMapping("/pedido/{token}/estado")
    public ResponseEntity<Map<String, Object>> verEstadoPedido(
            @PathVariable String token) {

        Pedido pedido = pedidoRepository
                .findByTokenCliente(token)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado"));

        return ResponseEntity.ok(Map.of(
                "pedidoId", pedido.getId(),
                "estado", pedido.getEstado().name(),
                "estadoAsignacion",
                pedido.getEstadoAsignacion().name(),
                "mesero", pedido.getMesero() != null
                        ? pedido.getMesero().getNombre()
                        : "Pendiente de asignación",
                "total", pedido.getTotal(),
                "detalles", pedido.getDetalles().stream()
                        .map(d -> Map.of(
                                "producto", d.getProducto().getNombre(),
                                "cantidad", d.getCantidad(),
                                "subtotal", d.getSubtotal()
                        )).toList()
        ));
    }
}