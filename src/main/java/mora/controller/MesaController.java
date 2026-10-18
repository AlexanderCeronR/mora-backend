package mora.controller;

import jakarta.validation.Valid;
import mora.dto.request.MesaRequest;
import mora.dto.response.MesaResponse;
import mora.entity.Mesa;
import mora.service.MesaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    // Todos los roles pueden ver las mesas
    public ResponseEntity<List<MesaResponse>> obtenerTodas() {
        List<MesaResponse> mesas = mesaService.obtenerTodas()
                .stream()
                .map(MesaResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(mesas);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    // Filtra mesas por estado: LIBRE, OCUPADA, RESERVADA
    public ResponseEntity<List<MesaResponse>> obtenerPorEstado(
            @PathVariable Mesa.Estado estado) {
        List<MesaResponse> mesas = mesaService.obtenerPorEstado(estado)
                .stream()
                .map(MesaResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(mesas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO', 'CAJERO')")
    public ResponseEntity<MesaResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                MesaResponse.fromEntity(mesaService.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    // Solo el ADMIN puede crear mesas
    public ResponseEntity<MesaResponse> crear(
            @Valid @RequestBody MesaRequest request) {
        Mesa mesa = Mesa.builder()
                .numero(request.getNumero())
                .capacidad(request.getCapacidad())
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                // 201 Created en vez de 200 OK
                // es la respuesta correcta para creación
                .body(MesaResponse.fromEntity(mesaService.crear(mesa)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MesaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MesaRequest request) {
        Mesa mesa = Mesa.builder()
                .numero(request.getNumero())
                .capacidad(request.getCapacidad())
                .build();
        return ResponseEntity.ok(
                MesaResponse.fromEntity(mesaService.actualizar(id, mesa)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    // PATCH porque solo actualizamos un campo
    // el MESERO puede cambiar estado pero no editar la mesa
    public ResponseEntity<MesaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Mesa.Estado estado) {
        return ResponseEntity.ok(
                MesaResponse.fromEntity(
                        mesaService.cambiarEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
        // 204 No Content es la respuesta correcta para eliminación
    }
}