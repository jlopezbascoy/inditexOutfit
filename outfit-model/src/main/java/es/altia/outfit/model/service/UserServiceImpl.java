package es.altia.outfit.model.service;

import es.altia.outfit.apimodel.service.ProductService;
import es.altia.outfit.apimodel.service.UserService;
import es.altia.outfit.apimodel.to.*;
import es.altia.outfit.model.model.PeticionUsuario;
import es.altia.outfit.model.model.ProductoOutfit;
import es.altia.outfit.model.model.RespuestaAI;
import es.altia.outfit.model.model.Usuario;
import es.altia.outfit.model.repository.PeticionUsuarioRepository;
import es.altia.outfit.model.repository.ProductoOutfitRepository;
import es.altia.outfit.model.repository.RespuestaAIRepository;
import es.altia.outfit.model.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PeticionUsuarioRepository peticionUsuarioRepository;
  private final RespuestaAIRepository respuestaAIRepository;
  private final ProductoOutfitRepository productoOutfitRepository;
  private final ProductService productService;

  public UserServiceImpl(UserRepository userRepository, PeticionUsuarioRepository peticionUsuarioRepository, RespuestaAIRepository respuestaAIRepository, ProductoOutfitRepository productoOutfitRepository, ProductService productService) {
    this.userRepository = userRepository;
    this.peticionUsuarioRepository = peticionUsuarioRepository;
    this.respuestaAIRepository = respuestaAIRepository;
    this.productoOutfitRepository = productoOutfitRepository;
    this.productService = productService;
  }

  @Override
  public UserTO registerUser(UserCreateTO userCreateTO) {
    //Falta validar si el usuario existe por nombre de usuario
    if(userRepository.existsByUsername(userCreateTO.getUsername())){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre de usuario ya existe");
    }
    Usuario usuario= new Usuario();
    usuario.setUsername(userCreateTO.getUsername());
    usuario.setPassword(userCreateTO.getPassword());
    userRepository.save(usuario);

    UserTO userTO= new UserTO();
    userTO.setUsername(usuario.getUsername());
    userTO.setId(usuario.getId());
    return userTO;
  }

  @Override
  public UserTO updateUser(UserUpdateTO userUpdateTO, Integer userId) {

    Usuario usuario= userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));
    usuario.setUsername(userUpdateTO.getUsername());
    usuario.setPassword(userUpdateTO.getPassword());
    userRepository.save(usuario);
    UserTO userTO= new UserTO();
    userTO.setId(usuario.getId());
    userTO.setUsername(usuario.getUsername());


    return userTO;
  }

  @Override
  public String deleteUser(UserDeleteTO userDeleteTO, Integer userId) {
    Usuario usuario= userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));
    if (usuario.getPassword().equals(userDeleteTO.getPassword())) {
      userRepository.deleteById(userId);
      return "Usuario eliminado correctamente";
    } else {
      return "La contraseña no es válida";
    }


  }

  @Override
  public List<OutfitResponseTO> listarOutfits(Integer userId) {
    Usuario usuario = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

    List<PeticionUsuario> peticionUsuarios = peticionUsuarioRepository.findByUsuarioId(userId);

    List<OutfitResponseTO> resultado = new ArrayList<>();

    for (PeticionUsuario peticion : peticionUsuarios) {
      String prompt = peticion.getPrompt();

      for (RespuestaAI respuesta : peticion.getRespuestasAI()) {
        Long respuestaId = respuesta.getId();

        List<ProductoOutfit> productoOutfits = productoOutfitRepository.findByRespuestaAI_Id(respuestaId);

        Set<String> productosAnadidos = new HashSet<>();
        List<ProductResponseTO> productos = new ArrayList<>();

        for (ProductoOutfit producto : productoOutfits) {
          if (productosAnadidos.contains(producto.getProductId())) {
            continue;
          }

          List<ProductResponseTO> prods = productService.getProductsAsTo(producto.getName());

          for (ProductResponseTO prod : prods) {
            if (!productosAnadidos.contains(prod.getId())) {
              productos.add(prod);
              productosAnadidos.add(prod.getId());
            }
          }
        }

        OutfitResponseTO outfit = new OutfitResponseTO(prompt, respuestaId, productos);
        resultado.add(outfit);
      }
    }

    return resultado;
  }

  @Override
  @Transactional
  public String borrarOutfit(Integer userId, Long outfitId, UserDeleteTO userDeleteTO) {
    Usuario usuario = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

    if (!usuario.getPassword().equals(userDeleteTO.getPassword())) {
      return "La contraseña no es válida";
    }

    RespuestaAI respuestaAI = respuestaAIRepository.findById(outfitId)
        .orElseThrow(() -> new EntityNotFoundException("No se encontró una RespuestaAI con ID " + outfitId));

    if (respuestaAI.getUsuario() == null || !respuestaAI.getUsuario().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este outfit no pertenece al usuario");
    }

    List<ProductoOutfit> productoOutfits = productoOutfitRepository.findByRespuestaAI_Id(outfitId);
    productoOutfitRepository.deleteAll(productoOutfits);

    PeticionUsuario peticion = respuestaAI.getPeticionUsuario();

    respuestaAIRepository.delete(respuestaAI);

    if (!respuestaAIRepository.existsByPeticionUsuario_Id(peticion.getId())) {
      peticionUsuarioRepository.delete(peticion);
    }

    return "Outfit eliminado correctamente";
  }

  @Override
  public OutfitResponseTO editarOutfit(Integer userId, Long outfitId, OutfitEditTO outfitEditTO) {
    Usuario usuario = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

    if (!usuario.getPassword().equals(outfitEditTO.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no es válida");
    }

    RespuestaAI respuestaAI = respuestaAIRepository.findById(outfitId)
        .orElseThrow(() -> new EntityNotFoundException("No se encontró una RespuestaAI con ID " + outfitId));

    if (respuestaAI.getUsuario() == null || !respuestaAI.getUsuario().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este outfit no pertenece al usuario");
    }

    List<String> addIds= outfitEditTO.getAddIds();
    List<String> deleteIds = outfitEditTO.getDeleteIds();
    List<ProductoOutfit> productoOutfits = productoOutfitRepository.findByRespuestaAI_Id(outfitId);

    if (deleteIds != null && !deleteIds.isEmpty()) {
      for (ProductoOutfit po : new ArrayList<>(productoOutfits)) {
        if (deleteIds.contains(po.getProductId())) {
          productoOutfitRepository.delete(po);
        }
      }
    }



  }


}
