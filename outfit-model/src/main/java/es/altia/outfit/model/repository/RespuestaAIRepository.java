package es.altia.outfit.model.repository;

import es.altia.outfit.model.model.RespuestaAI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespuestaAIRepository extends JpaRepository<RespuestaAI,Long> {
  List<RespuestaAI> findByUsuarioId(Integer userId);

  boolean existsByPeticionUsuario_Id(Long id);
}
