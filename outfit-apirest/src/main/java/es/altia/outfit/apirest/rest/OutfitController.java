package es.altia.outfit.apirest.rest;

import es.altia.outfit.OpenApiGenerator.DefaultApiDelegate;
import es.altia.outfit.apimodel.service.*;
import es.altia.outfit.apimodel.to.*;
import es.altia.outfit.apirest.mapper.OutfitRestMapper;
import es.altia.outfit.model.*;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class OutfitController implements DefaultApiDelegate {

  private final TokenService tokenService;
  private final OutfitRestMapper outfitRestMapper;
  private final ProductService productService;
  private final AIService aiService;
  private final TextIAService textIAService;
  private final UserService userService;

  public OutfitController(TokenService tokenService, OutfitRestMapper outfitRestMapper, ProductService productService, AIService aiService, TextIAService textIAService, UserService userService) {
    this.tokenService = tokenService;
    this.outfitRestMapper = outfitRestMapper;
    this.productService = productService;
    this.aiService = aiService;
    this.textIAService = textIAService;
    this.userService = userService;
  }

  @Override
  public ResponseEntity<TokenResponseDTO> authTokenPost() {
    TokenResponseTO to = tokenService.getAccessToken();
    TokenResponseDTO dto = outfitRestMapper.toDto(to);
    return ResponseEntity.ok(dto);
  }

  @Override
  public ResponseEntity<List<ProductDTO>> listarProductos(String query) {
    var productosTO = productService.getProductsAsTo(query);
    var productosDTO = outfitRestMapper.toDto(productosTO);
    return ResponseEntity.ok(productosDTO);
  }

  @Override
  public ResponseEntity<ChatResponseDTO> chatWithDeepseek(ChatRequestDTO chatRequestDTO) {
    chatRequestDTO.getMessages()
        .forEach(msg -> msg.setRole("user"));

    ChatRequestTO to = outfitRestMapper.toTo(chatRequestDTO);

    List<ProductResponseTO> productsTO = aiService.chat(to);

    List<ProductDTO> productsDTO = outfitRestMapper.toDto(productsTO);

    ChatResponseDTO resp = new ChatResponseDTO();
    resp.setResponse(productsDTO);

    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<ProductResponseClassifyDTO> chatClassify( @PathVariable("userId") Integer userId,
                                                                  @RequestBody ChatClassifyRequestDTO chatClassifyRequestDTO){
    String texto=chatClassifyRequestDTO.getText();
    List<ProductResponseTO> productsTO=textIAService.classify(texto,userId);
    List<ProductDTO> productsDTO = outfitRestMapper.toDto(productsTO);
    ProductResponseClassifyDTO productResponseClassifyDTO= new ProductResponseClassifyDTO();
    productResponseClassifyDTO.setResponse(productsDTO);
    return ResponseEntity.ok(productResponseClassifyDTO);


  }

  @Override
 public  ResponseEntity<UserDTO> registerUser(UserCreateDTO userCreateDTO){
   UserCreateTO userCreateTO= outfitRestMapper.toTo(userCreateDTO);
   UserTO userTO=userService.registerUser(userCreateTO);
   UserDTO userDTO=outfitRestMapper.toDto(userTO);
   return ResponseEntity.ok(userDTO);


  }

  @Override
  public ResponseEntity<UserDTO> editUser(Integer userId,
                                   UserUpdateDTO userUpdateDTO) {
    UserUpdateTO userUpdateTO=outfitRestMapper.toTo(userUpdateDTO);
    UserTO userTO=userService.updateUser(userUpdateTO,userId);
    UserDTO userDTO=outfitRestMapper.toDto(userTO);

    return ResponseEntity.ok(userDTO);
  }

  @Override
  public  ResponseEntity<String> deleteUser(Integer userId,
                                          UserDeleteDTO userDeleteDTO) {
    UserDeleteTO userDeleteTO=outfitRestMapper.toTo(userDeleteDTO);
    String respuesta= userService.deleteUser(userDeleteTO,userId);

    return ResponseEntity.ok(respuesta);
  }
  public  ResponseEntity<List<OutfitResponseDTO>> listarOutfitsPorUsuario(Integer userId) {
List<OutfitResponseTO> outfitResponseTOS=userService.listarOutfits(userId);
List<OutfitResponseDTO> outfitResponseDTOS=outfitRestMapper.toDtoList(outfitResponseTOS);
return ResponseEntity.ok(outfitResponseDTOS);
  }

  public ResponseEntity<String> deleteOutfit(Integer userId,
                                      Long outfitId,
                                      UserDeleteDTO userDeleteDTO) {
    UserDeleteTO userDeleteTO=outfitRestMapper.toTo(userDeleteDTO);
String respuesta=userService.borrarOutfit(userId,outfitId,userDeleteTO);
    return ResponseEntity.ok(respuesta);
  }
 public  ResponseEntity<OutfitResponseDTO> editOutfit(Integer userId,
                                               Long outfitId,
                                               OutfitEditDTO outfitEditDTO) {
OutfitEditTO outfitEditTO=outfitRestMapper.toTo(outfitEditDTO);
OutfitResponseTO outfitResponseTO=userService.editarOutfit(userId,outfitId,outfitEditTO);
OutfitResponseDTO outfitResponseDTO=outfitRestMapper.toDto(outfitResponseTO);
return ResponseEntity.ok(outfitResponseDTO);


 }
}

