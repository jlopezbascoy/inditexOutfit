package es.altia.outfit.apimodel.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutfitResponseTO {
  private String title;
  private Long id;
  private List<ProductResponseTO> productos;
}


