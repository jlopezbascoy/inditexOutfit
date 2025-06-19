package es.altia.outfit.model.service;

import es.altia.outfit.apimodel.service.TokenService;
import es.altia.outfit.apimodel.to.TokenResponseTO;
import es.altia.outfit.model.TokenResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

  private static final String GRANT_TYPE = "client_credentials";
  private static final String SCOPE      = "technology.catalog.read";

  private final RestTemplate restTemplate;
  private final String clientId;
  private final String clientSecret;
  private final String tokenUrl;

  public TokenServiceImpl(
      RestTemplateBuilder rb,
      @Value("${outfit.oauth2.client-id}") String clientId,
      @Value("${outfit.oauth2.client-secret}") String clientSecret,
      @Value("${outfit.oauth2.token-url}") String tokenUrl
  ) {
    this.restTemplate = rb.build();
    this.clientId     = clientId;
    this.clientSecret = clientSecret;
    this.tokenUrl     = tokenUrl;
  }

  @Override
  public TokenResponseTO getAccessToken() {
    log.info("Solicitando token a {} con grant_type={} y scope={}",
        tokenUrl, GRANT_TYPE, SCOPE);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", GRANT_TYPE);
    form.add("scope",      SCOPE);

    HttpEntity<MultiValueMap<String,String>> req = new HttpEntity<>(form, headers);

    ResponseEntity<TokenResponseTO> resp;
    try {
      resp = restTemplate.exchange(
          tokenUrl, HttpMethod.POST, req, TokenResponseTO.class
      );
    } catch (RestClientException ex) {
      log.error("Error al llamar a OAuth2: {}", ex.getMessage(), ex);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Error al solicitar token OAuth2", ex
      );
    }

    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new ResponseStatusException(
          resp.getStatusCode(),
          "Error obteniendo token: HTTP " + resp.getStatusCodeValue()
      );
    }

    return resp.getBody();
  }
}

