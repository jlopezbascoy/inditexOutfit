package es.altia.outfit.model.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class PeticionUsuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String prompt;

  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "peticionUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RespuestaAI> respuestasAI = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = true)
  private Usuario usuario;
}
