package es.altia.outfit.model.repository;

import es.altia.outfit.model.model.PeticionUsuario;
import es.altia.outfit.model.model.RespuestaAI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeticionUsuarioRepository extends JpaRepository<PeticionUsuario,Long>{
List<PeticionUsuario> findByUsuarioId(Integer userId);
}
