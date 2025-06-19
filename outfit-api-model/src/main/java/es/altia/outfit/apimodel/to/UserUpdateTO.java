package es.altia.outfit.apimodel.to;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateTO {
  private String password;
  private String username;

  public UserUpdateTO() {

  }
}
