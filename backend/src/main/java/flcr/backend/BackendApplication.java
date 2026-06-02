package flcr.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "flcr.backend.auth",
        "flcr.backend.common",
        "flcr.backend.community",
        "flcr.backend.ingredient",
        "flcr.backend.recipe",
        "flcr.backend.user"
})
@MapperScan("flcr.backend.*.mapper")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
