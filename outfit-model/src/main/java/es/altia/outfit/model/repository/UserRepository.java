package es.altia.outfit.model.repository;

import es.altia.outfit.model.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Usuario,Integer> {
  Optional<Usuario> findById(Integer id);
  boolean existsByUsername(String username);
}
