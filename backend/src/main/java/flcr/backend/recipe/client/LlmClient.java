package flcr.backend.recipe.client;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmClient {

    @Value("${llm.api.url}")
    private String apiUrl;

    @Value("${llm.api.model}")
    private String modelName;

    @Value("${llm.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    /**
     * 调用 LLM API 生成菜谱 JSON。
     *
     * @param promptContent 系统提示词
     * @return LLM 返回的 JSON 字符串
     * @throws BusinessException 当 API 调用失败或响应为空时
     */
    public String generateRecipeJson(String promptContent) {
        Map<String, Object> payload = buildPayload(promptContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map<String, Object>> responseEntity =
                    restTemplate.postForEntity(apiUrl, requestEntity, castToMapEntity());

            if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
                log.error("LLM API returned non-OK status: {}", responseEntity.getStatusCode());
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI服务响应异常");
            }

            String content = extractContent(responseEntity.getBody());
            if (content == null || content.isBlank()) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI服务返回为空");
            }
            return content;

        } catch (RestClientException e) {
            log.error("LLM API network error", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI服务网络请求失败");
        }
    }

    private Map<String, Object> buildPayload(String promptContent) {
        Map<String, String> systemMsg = Map.of("role", "system", "content", promptContent);
        Map<String, String> responseFormat = Map.of("type", "json_object");

        return Map.of(
                "model", modelName,
                "messages", List.of(systemMsg),
                "temperature", 0.7,
                "response_format", responseFormat
        );
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> body) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            return null;
        }
        return (String) message.get("content");
    }

    @SuppressWarnings("unchecked")
    private static Class<Map<String, Object>> castToMapEntity() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
