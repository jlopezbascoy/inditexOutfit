package es.altia.outfit.apimodel.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutfitEditTO {
  private String password;
  private List<String> addIds;
  private List<String> deleteIds;
}
