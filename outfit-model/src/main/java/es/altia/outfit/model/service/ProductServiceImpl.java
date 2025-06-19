package es.altia.outfit.model.service;

import es.altia.outfit.apimodel.service.ProductService;
import es.altia.outfit.apimodel.service.TokenService;
import es.altia.outfit.apimodel.to.ProductResponseTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final WebClient webClient;
    private final String productsUrl;
    private final TokenService tokenService;

    public ProductServiceImpl(
        @Value("${outfit.api.products-url}") String productsUrl,
        TokenService tokenService
    ) {
        this.productsUrl = productsUrl;
        this.tokenService = tokenService;
        this.webClient = WebClient.builder()
            .baseUrl(productsUrl)
            .defaultHeader(HttpHeaders.ACCEPT, "*/*")
            .build();
        log.info("WebClient inicializado con baseUrl={}", productsUrl);
    }

    @Override
    public List<ProductResponseTO> getProductsAsTo(String query) {
        log.info("Llamando a productos con query='{}'", query);

        var token = tokenService.getAccessToken();
        if (token == null || token.getIdToken() == null) {
            log.error("Token no obtenido o inválido");
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        try {
            return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("query", query)
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getIdToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("Error HTTP al llamar a Inditex: {}", clientResponse.statusCode());
                    return Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Error en API externa"));
                })
                .bodyToFlux(ProductResponseTO.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            log.error("Error HTTP al consumir API: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Error en API Inditex", ex);
        } catch (Exception e) {
            log.error("Error inesperado llamando al API de productos", e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado");
        }
    }
}
