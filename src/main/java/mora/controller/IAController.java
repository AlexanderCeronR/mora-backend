package mora.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ia")
public class IAController {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @PostMapping("/preguntar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<Map> preguntar(@RequestBody Map<String, String> body) {

        String pregunta = body.get("pregunta");
        String contexto = body.get("contexto");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> requestBody = Map.of(
                "model", "claude-sonnet-4-6",
                "max_tokens", 300,
                "system", contexto,
                "messages", List.of(
                        Map.of("role", "user", "content", pregunta)
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages",
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }
}