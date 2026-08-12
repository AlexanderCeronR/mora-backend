package mora.service;

import mora.entity.Mesa;
import mora.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    // Trae todas las mesas
    @Transactional(readOnly = true)
    // readOnly = true le dice a la BD que es solo lectura
    // mejora el rendimiento en consultas
    public List<Mesa> obtenerTodas() {
        return mesaRepository.findAll();
    }

    // Trae mesas por estado — útil para ver solo las libres
    @Transactional(readOnly = true)
    public List<Mesa> obtenerPorEstado(Mesa.Estado estado) {
        return mesaRepository.findByEstado(estado);
    }

    // Trae una mesa por id
    @Transactional(readOnly = true)
    public Mesa obtenerPorId(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Mesa no encontrada con id: " + id));
    }

    // Crea una mesa nueva
    @Transactional
    public Mesa crear(Mesa mesa) {
        // Verifica que no exista una mesa con ese número
        mesaRepository.findByNumero(mesa.getNumero())
                .ifPresent(m -> {
                    throw new RuntimeException(
                            "Ya existe la mesa número: " + mesa.getNumero());
                });
        return mesaRepository.save(mesa);
    }

    // Actualiza los datos de una mesa
    @Transactional
    public Mesa actualizar(Long id, Mesa mesaActualizada) {
        Mesa mesa = obtenerPorId(id);
        mesa.setNumero(mesaActualizada.getNumero());
        mesa.setCapacidad(mesaActualizada.getCapacidad());
        return mesaRepository.save(mesa);
    }

    // Cambia el estado de una mesa
    // Este es el método más usado en el día a día
    @Transactional
    public Mesa cambiarEstado(Long id, Mesa.Estado nuevoEstado) {
        Mesa mesa = obtenerPorId(id);
        mesa.setEstado(nuevoEstado);
        return mesaRepository.save(mesa);
    }

    // Elimina una mesa
    @Transactional
    public void eliminar(Long id) {
        Mesa mesa = obtenerPorId(id);
        mesaRepository.delete(mesa);
    }
}