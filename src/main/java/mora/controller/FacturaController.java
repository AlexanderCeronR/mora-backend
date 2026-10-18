package mora.controller;

import mora.entity.Pago;
import mora.repository.PagoRepository;
import mora.service.FacturaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    private final FacturaService facturaService;
    private final PagoRepository pagoRepository;

    public FacturaController(FacturaService facturaService,
                             PagoRepository pagoRepository) {
        this.facturaService = facturaService;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping("/{pagoId}/pdf")
    @PreAuthorize("isAuthenticated()")
    @Transactional // ← agrega esto
    public ResponseEntity<byte[]> descargarFactura(
            @PathVariable Long pagoId) {

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado"));

        try {
            byte[] pdf = facturaService.generarFacturaPDF(pago);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    pago.getNumeroFactura() + ".pdf"
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al generar la factura: " + e.getMessage());
        }
    }
}