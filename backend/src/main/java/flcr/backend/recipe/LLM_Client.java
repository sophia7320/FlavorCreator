import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LLMClient {

    // 【差异点1：常量与静态变量】
    private static final String API_URL = ""; 
    private static final String MODEL_NAME = ""; 
    private static final String API_KEY = "sk-vigcsnmtiayvgrwtudutzqvnqzrluppqppgrrrzryynvkmov";

    // 封装的 HTTP POST 请求方法
    // 【差异点4：数据提取与构造 (org.json 实战)】
    public String sendPostRequest(JSONArray messages) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("model", MODEL_NAME);
            payload.put("messages", messages);
            
            String payloadStr = payload.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payloadStr.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            JSONObject responseJson = new JSONObject(response.toString());
            String resultText = responseJson.getJSONArray("choices")
                                      .getJSONObject(0)
                                      .getJSONObject("message")
                                      .getString("content");
            return resultText;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
