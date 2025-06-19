package es.altia.outfit.apimodel.service;

import es.altia.outfit.apimodel.to.*;

import java.util.List;

public interface UserService {

   UserTO registerUser(UserCreateTO userCreateTO);
   UserTO updateUser(UserUpdateTO userUpdateTO,Integer userId);
   String deleteUser(UserDeleteTO userDeleteTO, Integer userId);
   List<OutfitResponseTO> listarOutfits(Integer userId);
   String borrarOutfit(Integer userId,Long outfitId,UserDeleteTO userDeleteTO);
   OutfitResponseTO editarOutfit(Integer userId,Long outfitId,OutfitEditTO outfitEditTO);
}
