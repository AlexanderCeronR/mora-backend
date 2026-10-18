package mora.repository;

import mora.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(Usuario.Rol rol);
    // Trae todos los meseros o supervisores

    List<Usuario> findByRolAndDisponibleTrue(Usuario.Rol rol);
    // Trae solo los meseros disponibles
}