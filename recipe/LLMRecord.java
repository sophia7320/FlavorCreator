import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LLMRecord {
    private String sessionId;
    private String userId;
    private LocalDateTime timestamp;
    private String userInput;
    private String llmResponse;
    private boolean wasBlocked;
}
