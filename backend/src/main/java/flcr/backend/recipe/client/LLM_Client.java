import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LLM_Client {

    @Value(" $ {llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value(" $ {llm.api.model:gpt-3.5-turbo}")
    private String modelName;

    @Value(" $ {llm.api.key:sk-vigcsnmtiayvgrwtudutzqvnqzrluppqppgrrrzryynvkmov}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendPostRequest(String promptContent) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", promptContent);
            messages.add(systemMsg);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", messages);
            payload.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                Map<String, Object> body = responseEntity.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "{\"recipe\": {\"name\": \"大模型接口调用失败\", \"ingredients\": [], \"steps\": [], \"cookTime\": 0, \"difficulty\": \"\", \"calories\": 0, \"tags\": []}}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"recipe\": {\"name\": \"网络请求异常\", \"ingredients\": [], \"steps\": [], \"cookTime\": 0, \"difficulty\": \"\", \"calories\": 0, \"tags\": []}}";
        }
    }
}
