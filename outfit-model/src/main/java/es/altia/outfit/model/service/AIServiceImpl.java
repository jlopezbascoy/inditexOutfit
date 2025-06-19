// src/main/java/es/altia/outfit/model/service/AIServiceImpl.java
package es.altia.outfit.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.altia.outfit.apimodel.service.AIService;
import es.altia.outfit.apimodel.service.ProductService;
import es.altia.outfit.apimodel.to.ChatRequestTO;
import es.altia.outfit.apimodel.to.ProductResponseTO;
import es.altia.outfit.model.model.PeticionUsuario;
import es.altia.outfit.model.model.RespuestaAI;
import es.altia.outfit.model.model.ProductoOutfit;
import es.altia.outfit.model.repository.PeticionUsuarioRepository;
import es.altia.outfit.model.repository.ProductoOutfitRepository;
import es.altia.outfit.model.repository.RespuestaAIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

  private final WebClient webClient;
  private final ObjectMapper mapper = new ObjectMapper();
  private final ProductService productService;
  private final PeticionUsuarioRepository peticionUsuarioRepository;
  private final RespuestaAIRepository respuestaAIRepository;
  private final ProductoOutfitRepository productoOutfitRepository;

  public AIServiceImpl(WebClient.Builder webClientBuilder,
                       ProductService productService,
                       PeticionUsuarioRepository peticionUsuarioRepository,
                       RespuestaAIRepository respuestaAIRepository,
                       ProductoOutfitRepository productoOutfitRepository) {
    this.webClient = webClientBuilder
        .baseUrl("http://localhost:11434")
        .build();
    this.productService = productService;
    this.peticionUsuarioRepository = peticionUsuarioRepository;
    this.respuestaAIRepository = respuestaAIRepository;
    this.productoOutfitRepository = productoOutfitRepository;
  }

  private List<String> convertirJsonAListaNombres() {
    return productService.getProductsAsTo("")
        .stream()
        .map(ProductResponseTO::getName)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductResponseTO> chat(ChatRequestTO request) {
    List<String> names = convertirJsonAListaNombres();
    String lista = String.join(",", names);
    String userPrompt = request.getMessages().get(0).getContent();

    if(!userPrompt.startsWith("Quiero un outfit para")){

      throw new IllegalArgumentException("Tu peticion debe comenzar por 'Quiero un outfit para'");

    }
    String instruction1 = "Elige entre las opciones de esta lista los productos para el outfit:";
    String instruction2 = "Devuelveme un listado con los nombres EXACTOS de los productos que escojas para el outfit, separados por comas, sin explicaciones.";
    String fullContent = userPrompt + " " + instruction1 + " " + lista + ". " + instruction2;
    log.info("Prompt final={}", fullContent);

    request.getMessages().clear();
    request.getMessages().add(new ChatRequestTO.ChatMessageTO("user", fullContent));
    Flux<JsonNode> flux = webClient.post()
        .uri("/api/chat")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.parseMediaType("application/x-ndjson")) // NDJSON(json lineas) La API va respondiendo en partes (streaming de datos). Querés procesar cada fragmento apenas llega, sin esperar toda la respuesta.
        .bodyValue(request)
        .retrieve()
        .bodyToFlux(String.class) //Convierte el cuerpo de la respuesta HTTP (que está en NDJSON) en un Flux<String>, es decir, un flujo de líneas de texto.
        .map(line -> {
          try {
            return mapper.readTree(line); //convertir cada línea JSON en un JsonNode, con el que puedes navegar mas facilmente y procesar su info para crear objetos java
          } catch (Exception e) {
            throw new RuntimeException("Error al parsear NDJSON", e);
          }
        });

    List<JsonNode> nodes = flux
        .takeUntil(node -> node.path("done").asBoolean(false))
        .collectList()
        .block();

    StringBuilder sb = new StringBuilder();
    if (nodes != null) {
      for (JsonNode node : nodes) {
        JsonNode content = node.path("message").path("content");
        if (!content.isMissingNode()) {
          sb.append(content.asText());
        }
      }
    }
    String respuestaRaw = sb.toString()
        .replaceAll("(?s)<think>.*?</think>", "")
        .replaceAll("\\\\n", " ")
        .replaceAll("[\\[\\]\\{\\}]", "")
        .replaceAll(" +", " ")
        .trim();
    log.info("Productos elegidos por la IA={}", respuestaRaw);

    String[] items = respuestaRaw.split(",");
    List<ProductResponseTO> allProducts = new ArrayList<>();
    for (String item : items) {
      String name = item.trim();
      if (name.isEmpty()) continue;
      List<ProductResponseTO> prods = productService.getProductsAsTo(name);
      allProducts.addAll(prods);
    }

    PeticionUsuario pet = new PeticionUsuario();
    pet.setPrompt(userPrompt);
    peticionUsuarioRepository.save(pet);

    RespuestaAI resAI = new RespuestaAI();
    resAI.setPeticionUsuario(pet);
    respuestaAIRepository.save(resAI);

    for (ProductResponseTO prod : allProducts) {
      ProductoOutfit po = new ProductoOutfit();
      po.setProductId(prod.getId());
      po.setName(prod.getName());
      po.setBrand(prod.getBrand());
      po.setLink(prod.getLink());
      if (prod.getPrice() != null) {
        po.setPriceCurrency(prod.getPrice().getCurrency());
        if (prod.getPrice().getValue() != null) {
          po.setPriceCurrent(BigDecimal.valueOf(prod.getPrice().getValue().getCurrent()));
          po.setPriceOriginal(BigDecimal.valueOf(
              prod.getPrice().getValue().getOriginal() != null
                  ? prod.getPrice().getValue().getOriginal()
                  : prod.getPrice().getValue().getCurrent()
          ));
        }
      }
      po.setRespuestaAI(resAI);
      productoOutfitRepository.save(po);
    }

    return allProducts;
  }
}
