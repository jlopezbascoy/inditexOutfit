package es.altia.outfit;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@OpenAPIDefinition(
    servers = {
        @Server(url = "http://api.outfit.altiacore.com", description = "Default Server URL"),
        @Server(url = "http://localhost:8080", description = "En local")
    }
)
@SpringBootApplication(scanBasePackages = "es.altia.outfit")
@EntityScan("es.altia.outfit.model")
public class OutfitApplication {

  public static void main(String[] args) {
    SpringApplication.run(OutfitApplication.class, args);
  }
}
