package es.altia.outfit.apimodel.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChatClassifyRequestTO {
  private String text;
}
