package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.PagoRequest;
import mora.dto.response.PagoResponse;
import mora.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    // Solo admin y cajero ven todos los pagos
    public ResponseEntity<List<PagoResponse>> obtenerTodos() {
        List<PagoResponse> pagos = pagoService.obtenerTodos()
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<PagoResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                PagoResponse.fromEntity(pagoService.obtenerPorId(id)));
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    // Ver el pago de un pedido específico
    public ResponseEntity<PagoResponse> obtenerPorPedido(
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(
                PagoResponse.fromEntity(
                        pagoService.obtenerPorPedido(pedidoId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    // El cajero registra el pago
    public ResponseEntity<PagoResponse> registrarPago(
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PagoResponse.fromEntity(
                        pagoService.registrarPago(request)));
    }
}