package es.altia.outfit.apimodel.service;

import es.altia.outfit.apimodel.to.ChatRequestTO;
import es.altia.outfit.apimodel.to.ProductResponseTO;

import java.util.List;

public interface AIService {

  List<ProductResponseTO> chat(ChatRequestTO request);
}
