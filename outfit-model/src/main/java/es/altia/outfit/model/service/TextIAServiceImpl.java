package es.altia.outfit.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.altia.outfit.apimodel.service.ProductService;
import es.altia.outfit.apimodel.service.TextIAService;
import es.altia.outfit.apimodel.to.ProductResponseTO;
import es.altia.outfit.model.model.PeticionUsuario;
import es.altia.outfit.model.model.ProductoOutfit;
import es.altia.outfit.model.model.RespuestaAI;
import es.altia.outfit.model.model.Usuario;
import es.altia.outfit.model.repository.PeticionUsuarioRepository;
import es.altia.outfit.model.repository.ProductoOutfitRepository;
import es.altia.outfit.model.repository.RespuestaAIRepository;
import es.altia.outfit.model.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TextIAServiceImpl implements TextIAService {

  private final ChatClient chatClient;
  private final ProductService productService;
  private final PeticionUsuarioRepository peticionUsuarioRepository;
  private final RespuestaAIRepository respuestaAIRepository;
  private final ProductoOutfitRepository productoOutfitRepository;
  private final UserRepository userRepository;

  TextIAServiceImpl(ChatClient.Builder chatClientBuilder, ProductService productService, PeticionUsuarioRepository peticionUsuarioRepository, RespuestaAIRepository respuestaAIRepository, ProductoOutfitRepository productoOutfitRepository, UserRepository userRepository) {
    this.chatClient = chatClientBuilder
        .defaultOptions(ChatOptions.builder()
            .temperature(0.0)
            .build())
        .build();
    this.productService = productService;
    this.peticionUsuarioRepository = peticionUsuarioRepository;
    this.respuestaAIRepository = respuestaAIRepository;
    this.productoOutfitRepository = productoOutfitRepository;
    this.userRepository = userRepository;
  }

  public List<ProductResponseTO> classify(String text,Integer userId) {

    Usuario usuario = userRepository.findById(userId)
        .orElseThrow(() ->
            new EntityNotFoundException("Usuario con ID " + userId + " no encontrado"));
    log.info("usuario={}",usuario);

    String respuestaIA= chatClient
        .prompt()
        .system("""
          Clasifica el texto en una de las siguientes clases.
         \s
          SI: Tiene que tener una intencion CLARA en la que explicitamente el usuario este preguntando que le escojas, crees, elijas... un outfit, indumentaria o cualquier sinonimo de estos dos ejemplos
         \s
          NO: Cualquier cosa que no encaje con lo anterior\s
         \s
         \s
             Text: Quiero un outfit para el fin de semana de fiesta
             Clase: SI
             Text: Escojeme ropa para un dia de sol
             Clase: SI
             Text: Dime que me puedo poner para ir mañana casual al trabajo
             Clase: SI
             Text: ¿Qué me pongo para una cena elegante?
                Clase: SI
                Text: Necesito que me armes un look para una boda
                Clase: SI
                Text: Elige por mí un outfit para ir al cine
                Clase: SI
                Text: ¿Podrías crear un conjunto de ropa para ir al gimnasio?
                Clase: SI
                Text: Quiero que me combines ropa para una reunión informal
                Clase: SI
                Text: Sugiereme un look para una salida nocturna
                Clase: SI
                Text: ¿Puedes elegir un conjunto para una entrevista de trabajo?
                Clase: SI
                Text: Diseñame un outfit para una sesión de fotos
                Clase: SI
                Text: ¿Qué me pondría para una cita romántica?
                Clase: SI
                Text: Hazme una propuesta de ropa para el domingo
                Clase: SI
                Text: ¿Qué outfit me recomiendas para un día en la playa?
                Clase: SI
                Text: Elige mi ropa para mañana
                Clase: SI
                Text: Combina un conjunto con jeans y camiseta
                Clase: SI
                Text: Dime qué ponerme para un evento formal esta noche
                   Clase: SI
                   Text: ¿Puedes elegir un outfit casual para una comida con amigos?
                   Clase: SI
                   Text: Escojeme ropa para una caminata en el campo
                   Clase: SI
                   Text: Hazme un conjunto con pantalón beige y camisa blanca
                   Clase: SI
                   Text: Elige por mí algo cómodo para viajar en avión
                   Clase: SI
                   Text: Sugiereme un outfit para una cita doble informal
                   Clase: SI
                   Text: Diseñame ropa para ir a un parque de diversiones
                   Clase: SI
                   Text: ¿Qué me pondría si llueve pero hace calor?
                   Clase: SI
                   Text: Me gustaría que me digas cómo vestirme para una entrevista online
                   Clase: SI
                   Text: Quiero que combines algo moderno y relajado para una tarde en el museo
                   Clase: SI
                   Text: Elige mi ropa para salir hoy en la noche
                   Clase: SI
                   Text: Crea un look para un brunch de domingo con amigas
                   Clase: SI
                   Text: Escógeme un conjunto relajado para trabajar desde casa
                   Clase: SI
             Text: Hablame sobre ropa
             Clase: NO
             Text: ¿Puedes hablarme sobre las mejores camisetas del mundo?
             Clase: NO
             Text: ¿Puedes decirme 5 personas dedicadas a la ropa?
             Clase: NO
             Text: ¿Que dia de la semana es mañana?
             Clase: NO
             Text: Hablame sobre los animales mas bonitos
             Clase: NO
             Text: ¿Cuál es tu color favorito?
                Clase: NO
                Text: ¿Cuántos estilos de ropa existen?
                Clase: NO
                Text: Me encanta la moda urbana
                Clase: NO
                Text: ¿Puedes hablarme sobre la historia del vestuario medieval?
                Clase: NO
                Text: ¿Qué piensas de la alta costura?
                Clase: NO
                Text: Las chaquetas de cuero son muy estilosas
                Clase: NO
                Text: ¿Qué opinas sobre la ropa sostenible?
                Clase: NO
                Text: ¿Quién diseña mejor, Dior o Chanel?
                Clase: NO
                Text: ¿Cuáles son las tendencias actuales?
                Clase: NO
                Text: ¿Puedes contarme sobre los tipos de pantalones que existen?
                Clase: NO
                Text: El algodón es un material muy cómodo
                Clase: NO
                Text: ¿Qué temperatura hace hoy?
                Clase: NO
                Text: ¿Puedes hablarme sobre moda japonesa?
                Clase: NO
                Text: ¿Cómo evolucionó el streetwear?
                Clase: NO
                Text: ¿Cuántos estilos de chaquetas conoces?
                   Clase: NO
                   Text: ¿Cuál es tu marca de ropa favorita?
                   Clase: NO
                   Text: Me interesa saber más sobre moda vegana
                   Clase: NO
                   Text: ¿Qué opinas del estilo boho?
                   Clase: NO
                   Text: ¿Puedes enseñarme sobre combinaciones de colores en moda?
                   Clase: NO
                   Text: ¿Cuáles son las mejores telas para el verano?
                   Clase: NO
                   Text: ¿Qué diseñador ganó el premio de moda este año?
                   Clase: NO
                   Text: ¿Cómo puedo aprender sobre diseño de moda?
                   Clase: NO
                   Text: Hablame sobre la historia de la moda en los años 90
                   Clase: NO
                   Text: ¿Qué tendencias vienen para el invierno?
                   Clase: NO
                   Text: ¿Puedes contarme sobre los looks de la Met Gala?
                   Clase: NO
                   Text: ¿Qué diferencia hay entre estilo y moda?
                   Clase: NO
                   Text: ¿Qué estilos de ropa son populares en Corea?
                   Clase: NO
                   Text: ¿Qué significa vestir con capas?
                   Clase: NO
           \s""")
        .user(text)
        .call()
        .content();

    log.info("Respuesta IA={}", respuestaIA);
    log.info("Prompt={}", text);

    if (respuestaIA.trim().equalsIgnoreCase("Clase: NO")) {
      throw new IllegalArgumentException("La petición debe tratarse sobre un outfit.");
    }

    List<String> names = productService.getProductsAsTo("").stream()
        .map(ProductResponseTO::getName)
        .collect(Collectors.toList());

    String lista = String.join(",", names);
    log.info("Lista productos={}", lista);

    String respuestaRaw = chatClient
        .prompt()
        .system("Elige entre las opciones de esta lista los productos para el outfit solicitado:\n"
            + lista + ". Devuelveme un listado con los nombres EXACTOS de los productos que "
            + "escojas para el outfit, separados por comas, sin explicaciones.")
        .user(text)
        .call()
        .content();

    String[] items = respuestaRaw.split(",");
    List<ProductResponseTO> allProducts = new ArrayList<>();
    Set<String> productosAnadidos = new HashSet<>();

    for (String item : items) {
      String name = item.trim();
      if (name.isEmpty()) continue;

      List<ProductResponseTO> prods = productService.getProductsAsTo(name);
      for (ProductResponseTO prod : prods) {
        if (productosAnadidos.contains(prod.getId())) {
          continue;
        }
        allProducts.add(prod);
        productosAnadidos.add(prod.getId());
      }
    }

    PeticionUsuario pet = new PeticionUsuario();
    pet.setPrompt(text);
    pet.setUsuario(usuario);
    peticionUsuarioRepository.save(pet);

    RespuestaAI resAI = new RespuestaAI();
    resAI.setPeticionUsuario(pet);

    ObjectMapper mapper = new ObjectMapper();
    String json;
    try {
      json = mapper.writeValueAsString(allProducts);
      resAI.setResponseText(json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      resAI.setResponseText("[]");
    }
    resAI.setUsuario(usuario);
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

  private List<String> convertirJsonAListaNombres() {
    return productService.getProductsAsTo("")
        .stream()
        .map(ProductResponseTO::getName)
        .collect(Collectors.toList());
  }


}
