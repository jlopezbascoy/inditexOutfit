package es.altia.outfit.apirest.mapper;


import es.altia.outfit.apimodel.to.*;
import es.altia.outfit.model.*;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OutfitRestMapper
{
TokenResponseDTO toDto (TokenResponseTO to);
List<ProductDTO> toDto (List<ProductResponseTO> to);
ChatRequestTO toTo (ChatRequestDTO dto);
UserCreateTO toTo (UserCreateDTO dto);
UserDTO toDto (UserTO userTO);
UserUpdateTO toTo (UserUpdateDTO userUpdateDTO);
UserDeleteTO toTo (UserDeleteDTO userDeleteDTO);
List<OutfitResponseDTO> toDtoList (List<OutfitResponseTO> outfitResponseTOS);
OutfitEditTO toTo(OutfitEditDTO outfitEditDTO);
OutfitResponseDTO toDto(OutfitResponseTO outfitResponseTO);
}


