import .recipe.LLMRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Slf4j
@Service
public class RecipeService {
    /**
     * 核心方法：调用 Python 脚本并记录日志
     * @param food 食材
     * @param demand 要求
     * @param sessionId 会话ID（实际项目中可以从前端请求头或登录态获取）
     * @param userId 用户ID
     * @return AI 生成的食谱或拦截提示
     */
    public String generateRecipe(String food, String demand, String sessionId, String userId) {
        // 1. 初始化日志记录对象（复刻 Python 的 LLMRecord）
        LLMRecord record = new LLMRecord();
        record.setSessionId(sessionId);
        record.setUserId(userId);
        record.setTimestamp(LocalDateTime.now());
        record.setUserInput("食材：" + food + "，要求：" + demand);
        record.setWasBlocked(false);

        StringBuilder result = new StringBuilder();

        try {
            // 2. 配置 ProcessBuilder 调用你的 py_ja.py 脚本
            // 注意：这里的路径是相对路径，假设 py_ja.py 放在项目根目录的 python 文件夹下
            String scriptPath = "python/py_ja.py";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", scriptPath, food, demand
            );

            // 合并错误输出，方便排查 Python 脚本报错
            processBuilder.redirectErrorStream(true);

            // 3. 启动 Python 进程
            Process process = processBuilder.start();

            // 4. 读取 Python 脚本 print 出来的结果
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            // 等待脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                record.setWasBlocked(true);
                record.setLlmResponse("Python 脚本执行异常，退出码：" + exitCode);
            } else {
                String responseText = result.toString();
                record.setLlmResponse(responseText);
                // 复刻 Python 中的拦截判断逻辑：如果返回了拦截提示，标记为已拦截
                if (responseText.contains("食材输入有误") || responseText.contains("要求与食谱无关")) {
                    record.setWasBlocked(true);
                }
            }

        } catch (Exception e) {
            log.error("调用 Python 服务发生严重异常", e);
            record.setWasBlocked(true);
            record.setLlmResponse("系统繁忙，调用 AI 失败：" + e.getMessage());
        } finally {
            // 5. 无论成功还是失败，都保存审计日志（复刻 Python 的 save_record）
            saveRecord(record);
        }

        return record.getLlmResponse();
    }

    /**
     * 保存日志（复刻 Python 的 save_record）
     * 这里使用日志打印来模拟，实际项目中你可以替换为真实的数据库插入（如 JDBC 或 MyBatis）
     */
    private void saveRecord(LLMRecord record) {
        log.info("【LLM审计日志】会话ID:{}, 用户ID:{}, 拦截状态:{}, 用户输入:{}, AI回复:{}",
                record.getSessionId(), record.getUserId(), record.isWasBlocked(), record.getUserInput(), record.getLlmResponse());

        // 如果你有数据库，可以在这里写：db.insert("llm_logs", record);
    }
}
