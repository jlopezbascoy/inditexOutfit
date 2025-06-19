package es.altia.outfit.model.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
public class RespuestaAI {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Lob
  @Column(name = "response_text", columnDefinition = "TEXT")
  private String responseText;
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne
  @JoinColumn(name = "peticion_usuario_id", nullable = false)
  private PeticionUsuario peticionUsuario;

  @OneToMany(mappedBy = "respuestaAI", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductoOutfit> productosOutfits = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = true)
  private Usuario usuario;
}
