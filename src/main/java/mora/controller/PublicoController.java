package mora.controller;

import mora.dto.request.DetallePedidoRequest;
import mora.dto.response.PedidoResponse;
import mora.dto.response.ProductoResponse;
import mora.entity.*;
import mora.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/publico")
@CrossOrigin(origins = "*")
public class PublicoController {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;

    public PublicoController(
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            MesaRepository mesaRepository) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
    }

    // Ver menú disponible — sin auth
    @GetMapping("/menu")
    public ResponseEntity<List<ProductoResponse>> verMenu() {
        List<ProductoResponse> productos = productoRepository
                .findByDisponibleTrue()
                .stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(productos);
    }

    // Ver menú por categoría — sin auth
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

    // Hacer pedido desde el QR — sin auth
    @PostMapping("/mesa/{mesaId}/pedido")
    public ResponseEntity<Map<String, Object>> hacerPedido(
            @PathVariable Long mesaId,
            @RequestBody List<DetallePedidoRequest> detalles) {

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException(
                        "Mesa no encontrada"));

        // Crea el pedido sin mesero asignado
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
                        "Producto no disponible: " + producto.getNombre());
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

        // Cambia la mesa a OCUPADA
        mesa.setEstado(Mesa.Estado.OCUPADA);
        mesaRepository.save(mesa);

        Pedido guardado = pedidoRepository.save(pedido);

        // Retorna el token para que el cliente
        // pueda ver el estado de su pedido
        return ResponseEntity.ok(Map.of(
                "pedidoId", guardado.getId(),
                "tokenCliente", guardado.getTokenCliente(),
                "total", guardado.getTotal(),
                "mensaje", "Pedido recibido. " +
                        "Un mesero será asignado pronto."
        ));
    }

    // Ver estado del pedido — solo con token del cliente
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