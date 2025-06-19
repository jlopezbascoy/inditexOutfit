package es.altia.outfit.model.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URI;

@Entity
@Data
public class ProductoOutfit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String productId;

  private String name;

  private String brand;

  private URI link;

  private String priceCurrency;

  private BigDecimal priceCurrent;

  private BigDecimal priceOriginal;

  @ManyToOne
  @JoinColumn(name = "ai_response_id", nullable = false)
  private RespuestaAI respuestaAI;
}