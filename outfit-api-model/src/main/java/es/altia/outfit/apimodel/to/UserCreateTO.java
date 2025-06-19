package es.altia.outfit.apimodel.to;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreateTO {
  private String username;
  private String password;
}
