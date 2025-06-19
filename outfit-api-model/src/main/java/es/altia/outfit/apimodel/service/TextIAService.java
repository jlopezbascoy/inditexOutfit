package es.altia.outfit.apimodel.service;

import es.altia.outfit.apimodel.to.ProductResponseTO;

import java.util.List;

public interface TextIAService {
   List<ProductResponseTO> classify(String text, Integer userId);
}
