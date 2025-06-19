package es.altia.outfit.model.repository;

import es.altia.outfit.model.model.ProductoOutfit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoOutfitRepository extends JpaRepository<ProductoOutfit,Long> {
  List<ProductoOutfit>  findByRespuestaAI_Id (Long id);
}
